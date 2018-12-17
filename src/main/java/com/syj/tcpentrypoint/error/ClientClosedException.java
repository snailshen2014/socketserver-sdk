package com.syj.tcpentrypoint.error;



/**
 * Title: 客户端连接断开异常<br>
 * <p/>
 * Description: <br>
 * <p/>
 */
public class ClientClosedException extends RpcException {

    /**
     * The constant serialVersionUID.
     */
    private static final long serialVersionUID = -8023971086755745412L;

    /**
     * Instantiates a new Client closed exception.
     */
    protected ClientClosedException() {
    }

    /**
     * Instantiates a new Client closed exception.
     *
     * @param errorMsg
     *         the error msg
     */
    public ClientClosedException(String errorMsg) {
        super(errorMsg);
    }

    /**
     * Instantiates a new Client closed exception.
     *
     * @param errorMsg
     *         the error msg
     * @param throwable
     *         the throwable
     */
    public ClientClosedException(String errorMsg, Throwable throwable) {
        super(errorMsg, throwable);
    }

}