package com.syj.tcpentrypoint.error;



/**
 * Title: 客户端等待超时的异常 <br>
 * <p/>
 * Description: <br>
 * <p/>
 */
public class ClientEndpointTimeoutException extends RpcException {

    /**
     * The constant serialVersionUID.
     */
    private static final long serialVersionUID = -3008927155169307876L;

    /**
     * Instantiates a new Client timeout exception.
     */
    protected ClientEndpointTimeoutException() {
    }

    /**
     * Instantiates a new Client timeout exception.
     *
     * @param errorMsg
     *         the error msg
     */
    public ClientEndpointTimeoutException(String errorMsg) {
        super(errorMsg);
    }

    /**
     * Instantiates a new Client timeout exception.
     *
     * @param errorMsg
     *         the error msg
     * @param throwable
     *         the throwable
     */
    public ClientEndpointTimeoutException(String errorMsg, Throwable throwable) {
        super(errorMsg, throwable);
    }
}