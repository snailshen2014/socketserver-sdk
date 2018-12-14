package com.syj.tcpentrypoint.error;



import java.io.Serializable;

/**
 * 
*  @des    :The exception of the serialization
 * @author:shenyanjun1
 * @date   :2018-12-14 17:02
 */
public class RECodecException extends RpcException implements Serializable {

    /**
     * The constant serialVersionUID.
     */
    private static final long serialVersionUID = -3048764230942133763L;

    /**
     * Instantiates a new RE codec exception.
     */
    protected RECodecException() {
    }

    /**
     * Instantiates a new RE codec exception.
     *
     * @param errorMsg
     *         the error msg
     */
    public RECodecException(String errorMsg) {
        super(errorMsg);
    }

    /**
     * Instantiates a new RE codec exception.
     *
     * @param errorMsg
     *         the error msg
     * @param e
     *         the e
     */
    public RECodecException(String errorMsg, Throwable e) {
        super(errorMsg, e);
    }

}