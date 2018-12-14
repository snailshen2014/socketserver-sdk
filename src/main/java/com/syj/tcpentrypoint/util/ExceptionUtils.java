package com.syj.tcpentrypoint.util;

import com.syj.tcpentrypoint.error.RpcException;
import com.syj.tcpentrypoint.msg.MessageHeader;

/**
 * Title:异常处理工具类<br>
 * <p/>
 * Description: <br>
 * <p/>
 */
public final class ExceptionUtils {

    /**
     * 返回堆栈信息（e.printStackTrace()的内容）
     *
     * @param e
     *         Throwable
     * @return 异常堆栈信息
     */
    public static String toString(Throwable e) {
        StackTraceElement[] traces = e.getStackTrace();
        StringBuilder sb = new StringBuilder(1024);
        sb.append(e.toString()).append("\n");
        if (traces != null) {
            for (StackTraceElement trace : traces) {
                sb.append("\tat ").append(trace).append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * 返回消息+简短堆栈信息（e.printStackTrace()的内容）
     *
     * @param e
     *         Throwable
     * @param stackLevel
     *         堆栈层级
     * @return 异常堆栈信息
     */
    public static String toShortString(Throwable e, int stackLevel) {
        StackTraceElement[] traces = e.getStackTrace();
        StringBuilder sb = new StringBuilder(1024);
        sb.append(e.toString()).append("\t");
        if (traces != null) {
            for (int i = 0; i < traces.length; i++) {
                if (i < stackLevel) {
                    sb.append("\tat ").append(traces[i]).append("\t");
                } else {
                    break;
                }
            }
        }
        return sb.toString();
    }

    /**
     * 封装RpcException
     *
     * @param header
     *         消息头
     * @param throwable
     *         异常
     * @return RpcException
     */
    public static RpcException handlerException(MessageHeader header, Throwable throwable){
        RpcException exception = null;
        if(throwable instanceof RpcException){
            exception = (RpcException) throwable;
            if(header != null) exception.setMsgHeader(header);

        }else{
            exception = new RpcException(header,throwable);

        }
        return  exception;
    }
}