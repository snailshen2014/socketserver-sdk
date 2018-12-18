package com.syj.tcpentrypoint.error;



/**
 * Title: 客户端等待超时的异常 <br>
 * <p/>
 * Description: <br>
 * <p/>
 */
public class ClientTimeoutException extends RpcException {

    /**
     * The constant serialVersionUID.
     */
    private static final long serialVersionUID = -3008927155169307876L;

    /**
     * Instantiates a new Client timeout exception.
     */
    protected ClientTimeoutException() {
    }

    /**
     * Instantiates a new Client timeout exception.
     *
     * @param errorMsg
     *         the error msg
     */
    public ClientTimeoutException(String errorMsg) {
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
    public ClientTimeoutException(String errorMsg, Throwable throwable) {
        super(errorMsg, throwable);
    }
}