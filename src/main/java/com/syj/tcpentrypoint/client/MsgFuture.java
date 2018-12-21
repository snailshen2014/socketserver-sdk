package com.syj.tcpentrypoint.client;




import java.util.Date;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.syj.tcpentrypoint.error.ClientEndpointTimeoutException;
import com.syj.tcpentrypoint.error.RpcException;
import com.syj.tcpentrypoint.msg.MessageHeader;
import com.syj.tcpentrypoint.msg.ResponseMessage;
import com.syj.tcpentrypoint.util.DateUtils;
import com.syj.tcpentrypoint.util.NetUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

/**
 * Title: 返回消息Future <br>
 * <p/>
 * Description: 包含了get方法，同步等待返回<br>
 * <p/>
 */
public class MsgFuture<V> implements java.util.concurrent.Future<V>{

    /**
     * slf4j logger
     */
    private final static Logger logger = LoggerFactory.getLogger(MsgFuture.class);

    /**
     * 结果监听器，在返回成功或者异常的时候需要通知
     */
//    private List<ResultListener> listeners = new ArrayList<ResultListener>();

    private volatile Object result;

    private short waiters;

    private static final String UNCANCELLABLE = "UNCANCELLABLE";

    private static final CauseHolder CANCELLATION_CAUSE_HOLDER  = new CauseHolder(new CancellationException());

    /**
     * 当前连接
     */
    private final Channel channel;
    /**
     * 当前消息头
     */
    private final MessageHeader header;
    /**
     * 用户设置的超时时间
     */
    private final int timeout;
    /**
     * Future生成时间
     */
    private final long genTime = System.currentTimeMillis();
    /**
     * Future已发送时间
     */
    private volatile long sentTime;
    /**
     * Future完成的时间
     */
    private volatile long doneTime = 0l;
    /**
     * 是否同步调用，默认是
     */
    private boolean asyncCall;

    /**
     * 构造函数
     *
     * @param channel
     *         连接
     * @param header
     *         消息头
     * @param timeout
     *         超时时间
     */
    public MsgFuture(Channel channel, MessageHeader header, int timeout) {
        this.channel = channel;
        this.header = header;
        this.timeout = timeout;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        boolean res = this.cancle0(mayInterruptIfRunning);
//        notifyListeners();
        return res;
    }

    private boolean cancle0(boolean mayInterruptIfRunning){
        Object result = this.result;
        if (isDone0(result) || result == UNCANCELLABLE) {
            return false;
        }
        synchronized (this) {
            // Allow only once.
            result = this.result;
            if (isDone0(result) || result == UNCANCELLABLE) {
                return false;
            }
            this.result = CANCELLATION_CAUSE_HOLDER;
            this.setDoneTime();
            if (hasWaiters()) {
                notifyAll();
            }
        }
        return true;
    }

    @Override
    public boolean isCancelled() {
        return this.result == CANCELLATION_CAUSE_HOLDER;
    }

    @Override
    public boolean isDone() {
        return isDone0(result);
    }

    private static boolean isDone0(Object result) {
        return result != null && result != UNCANCELLABLE;
    }

    @Override
    public V get() throws InterruptedException {
        return get(timeout, TimeUnit.MILLISECONDS);
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException{
        timeout = unit.toMillis(timeout); // 转为毫秒
        long remaintime = timeout - (sentTime - genTime); // 剩余时间
        if (remaintime <= 0) { // 没有剩余时间不等待
            if (isDone()) { // 直接看是否已经返回
                return getNow();
            }
        } else { // 等待剩余时间
            if (await(remaintime, TimeUnit.MILLISECONDS)) {
                return getNow();
            }
        }
        this.setDoneTime();
        throw clientTimeoutException(false);
    }

    /**
     * 构建超时异常
     *
     * @param scan
     *         是否扫描线程
     * @return 异常ClientTimeoutException
     */
    public ClientEndpointTimeoutException clientTimeoutException(boolean scan) {
        Date now = new Date();
        String errorMsg = (sentTime > 0 ? "Waiting provider return response timeout"
                : "Consumer send request timeout")
                + ". Start time: " + DateUtils.dateToMillisStr(new Date(genTime))
                + ", End time: " + DateUtils.dateToMillisStr(now)
                + ((sentTime > 0 ?
                ", Client elapsed: " + (sentTime - genTime)
                        + "ms, Server elapsed: " + (now.getTime() - sentTime)
                : ", Client elapsed: " + (now.getTime() - genTime))
                + "ms, Timeout: " + timeout
                + "ms, MsgHeader: " + this.header
                + ", Channel: " + NetUtils.channelToString(channel.localAddress(), channel.remoteAddress()))
                + (scan ? ", throws by scan thread" : ".");
        return new ClientEndpointTimeoutException(errorMsg);
    }

    public boolean isSuccess() {
        Object result = this.result;
        if (result == null ) {
            return false;
        }
        return !(result instanceof CauseHolder);
    }

    public Throwable cause() {
        Object result = this.result;
        if (result instanceof CauseHolder) {
            return ((CauseHolder) result).cause;
        }
        return null;
    }

    public V getNow() {
        Object result = this.result;
        if (result instanceof ResponseMessage) { // 服务端返回
            ResponseMessage tmp = (ResponseMessage) result;
            if (tmp.getMsgBody() != null) {
                synchronized (this) {
                    if (tmp.getMsgBody() != null) {
                        try {
//                            ResponseMessage ins = (ResponseMessage) protocol.decode(tmp.getMsgBody(), ResponseMessage.class.getCanonicalName());
//                            if (ins.getResponse() != null) {
//                                tmp.setResponse(ins.getResponse());
//                            } else if (ins.getException() != null) tmp.setException(ins.getException());
                        } finally {
                            if (tmp.getMsgBody() != null) {
                                tmp.getMsgBody().release();
                            }
                            tmp.setMsgBody(null); // 防止多次调用get方法触发反序列化异常
                        }
                    }
                }
            }
        } else if (result instanceof CauseHolder) { // 本地异常
            Throwable e = ((CauseHolder) result).cause;
            if (e instanceof RpcException) {
                RpcException rpcException = (RpcException) e;
                rpcException.setMsgHeader(header);
                throw rpcException;
            } else {
                throw new RpcException(this.header, ((CauseHolder) result).cause);
            }
        }
        return (V) result;
    }

    public void releaseIfNeed() {
        this.releaseIfNeed0((V) result);
    }

    private synchronized void releaseIfNeed0(V result){
        if(result instanceof ResponseMessage){
            ByteBuf byteBuf = ((ResponseMessage) result).getMsgBody();
            if(byteBuf != null && byteBuf.refCnt() > 0 ){
                byteBuf.release();
            }
        }
    }

    private static final class CauseHolder {
        final Throwable cause;
        private CauseHolder(Throwable cause) {
            this.cause = cause;
        }
    }

    public boolean await(long timeout, TimeUnit unit)
            throws InterruptedException {
        return await0(unit.toNanos(timeout), true);
    }

    private boolean await0(long timeoutNanos, boolean interruptable) throws InterruptedException {
        if (isDone()) {
            return true;
        }

        if (timeoutNanos <= 0) {
            return isDone();
        }

        if (interruptable && Thread.interrupted()) {
            throw new InterruptedException(toString());
        }

        long startTime = System.nanoTime();
        long waitTime = timeoutNanos;
        boolean interrupted = false;

        try {
            synchronized (this) {
                if (isDone()) {
                    return true;
                }

                if (waitTime <= 0) {
                    return isDone();
                }

                //checkDeadLock(); need this check?
                incWaiters();
                try {
                    for (;;) {
                        try {
                            wait(waitTime / 1000000, (int) (waitTime % 1000000));
                        } catch (InterruptedException e) {
                            if (interruptable) {
                                throw e;
                            } else {
                                interrupted = true;
                            }
                        }

                        if (isDone()) {
                            return true;
                        } else {
                            waitTime = timeoutNanos - (System.nanoTime() - startTime);
                            if (waitTime <= 0) {
                                return isDone();
                            }
                        }
                    }
                } finally {
                    decWaiters();
                }
            }
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }


    public MsgFuture<V> setSuccess(V result) {
        if(this.isCancelled()){
            this.releaseIfNeed0(result);
            return this;
        }
        if (setSuccess0(result)) {
//            notifyListeners();
            return this;
        }
        throw new IllegalStateException("complete already: " + this);
    }

    private boolean setSuccess0(V result) {
        if (isDone()) {
            return false;
        }
        synchronized (this) {
            // Allow only once.
            if (isDone()) {
                return false;
            }
            if (this.result == null) {
                this.result = result;
                this.setDoneTime();
            }
            if (hasWaiters()) {
                notifyAll();
            }
        }
        return true;
    }


    public MsgFuture<V> setFailure(Throwable cause) {
        if(this.isCancelled()){
            this.releaseIfNeed();
            return this;
        }
        if (setFailure0(cause)) {
//            notifyListeners();
            return this;
        }
        throw new IllegalStateException("complete already: " + this, cause);
    }

    private boolean setFailure0(Throwable cause) {
        if (isDone()) {
            return false;
        }

        synchronized (this) {
            // Allow only once.
            if (isDone()) {
                return false;
            }
            result = new CauseHolder(cause);
            this.setDoneTime();
            if (hasWaiters()) {
                notifyAll();
            }
        }
        return true;
    }

   

    private boolean hasWaiters() {
        return waiters > 0;
    }

    private void incWaiters() {
        if (waiters == Short.MAX_VALUE) {
            throw new IllegalStateException("too many waiters: " + this);
        }
        waiters ++;
    }

    private void decWaiters() {
        waiters --;
    }

    /**
     * 查看生成时间
     *
     * @return 生成时间
     */
    public long getGenTime() {
        return genTime;
    }

    /**
     * 查看超时时间
     *
     * @return 超时时间
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     *  查看完成时间
     * @return  完成时间
     */
    public long getDoneTime() {
        if (doneTime == 0l) {
            long remaintime = timeout - (sentTime - genTime); // 剩余时间
            if (remaintime <= 0) { // 没有剩余时间
                doneTime = System.currentTimeMillis();
            }
        }
        return doneTime;
    }

    private void setDoneTime() {
        if (doneTime == 0l) {
            doneTime = System.currentTimeMillis();
        }
    }

    /**
     * 设置已发送时间
     *
     * @param sentTime
     *         已发送时间
     */
    public void setSentTime(long sentTime) {
        this.sentTime = sentTime;
    }

    /**
     * 是否异步调用
     *
     * @return 是否异步调用
     */
    public boolean isAsyncCall() {
        return asyncCall;
    }

    /**
     * 标记为异步调用
     *
     * @param asyncCall
     *         是否异步调用
     */
    public void setAsyncCall(boolean asyncCall) {
        this.asyncCall = asyncCall;
    }
   
}