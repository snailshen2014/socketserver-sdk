package com.syj.tcpentrypoint.error;
import java.io.Serializable;
import com.syj.tcpentrypoint.msg.MessageHeader;

/**
 * 
*  @des    :The exception of the calling
 * @author:shenyanjun1
 * @date   :2018-12-14 16:52
 */
public class RpcException extends RuntimeException implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 7772360050382788166L;

    private int errorCode = 2;

    protected String errorMsg;

    private transient MessageHeader msgHeader;

    // 需要序列化支持
    protected RpcException() {
    }


    public RpcException(MessageHeader header, Throwable e) {
        super(e);
        this.msgHeader = header;

    }

    public RpcException(MessageHeader header, String errorMsg) {
        super(errorMsg);
        this.msgHeader = header;
        this.errorMsg = errorMsg;
    }

    protected RpcException(Throwable e) {
        super(e);
    }

    public RpcException(String errorMsg) {
        super(errorMsg);
        this.errorMsg = errorMsg;
    }

    public RpcException(String errorMsg, Throwable e) {
        super(errorMsg, e);
        this.errorMsg = errorMsg;
    }

    public MessageHeader getMsgHeader() {
        return msgHeader;
    }

    public void setMsgHeader(MessageHeader msgHeader) {
        this.msgHeader = msgHeader;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }


    public String toString() {
        String s = getClass().getName();
        String message = this.errorMsg;
        return (message != null) ? (s + ": " + message) : s;
    }


}