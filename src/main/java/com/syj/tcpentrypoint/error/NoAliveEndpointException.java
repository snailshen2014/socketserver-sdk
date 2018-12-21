package com.syj.tcpentrypoint.error;



import java.util.Collection;

/**
 * Title: 没有可用的服务端 <br>
 * <p/>
 * Description: <br>
 * <p/>
 */
public class NoAliveEndpointException extends RpcException {

    /**
     * The constant serialVersionUID.
     */
    private static final long serialVersionUID = 399321029655641392L;

    /**
     * Instantiates a new No alive provider exception.
     *
     * @param key
     *         the key
     * @param serverIp
     *         the server ip
     */
    public NoAliveEndpointException(String key, String serverIp) {
        super("No alive provider of pinpoint address : [" + serverIp + "]! The key is " + key);
    }

    /**
     * Instantiates a new No alive provider exception.
     *
     * @param key
     *         the key
     * @param providers
     *         the providers
     */
    public NoAliveEndpointException(String key, Collection providers) {
        super("No alive provider! The key is " + key + ", current providers is " + providers);
    }

    /**
     * Instantiates a new No alive provider exception.
     */
    protected NoAliveEndpointException() {

    }
}