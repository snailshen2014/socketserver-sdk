package com.syj.tcpentrypoint.client;

import java.util.List;
import com.syj.tcpentrypoint.config.ClientEndpointConfig;
import com.syj.tcpentrypoint.error.IllegalConfigureException;
import com.syj.tcpentrypoint.error.NoAliveEndpointException;
import com.syj.tcpentrypoint.util.Constants;

/**
 * 
*  @des    :负载均衡算法基类+工厂类
 * @author:shenyanjun1
 * @date   :2018-12-20 16:52
 */
public abstract class Loadbalance {

    /**
     * 一些客户端的配置
     */
    protected ClientEndpointConfig consumerConfig;

    /**
     * 得到负载均衡算法
     *
     * @param loadBalanceName
     *         负载均衡名称
     * @return Loadbalance实现 loadbalance
     */
    public static Loadbalance getInstance(String loadBalanceName) {
        if (Constants.LOADBALANCE_RANDOM.equals(loadBalanceName)) {
            return new RandomLoadbalance();
        } 
        /*else if (Constants.LOADBALANCE_ROUNDROBIN.equals(loadBalanceName)) {
            return new RoundrobinLoadbalance();
        } else if (Constants.LOADBALANCE_LEASTACTIVE.equals(loadBalanceName)) {
            return new LeastActiveLoadbalance();
        } else if (Constants.LOADBALANCE_CONSISTENTHASH.equals(loadBalanceName)) {
            return new ConsistentHashLoadbalance();
        } else if (Constants.LOADBALANCE_LOCALPREF.equals(loadBalanceName)) {
            return new LocalPreferenceLoadbalance();
        }*/ else {
            // 非法配置
            throw new IllegalConfigureException(100011, "consumer.loadbalance",
                    loadBalanceName);
        }
    }

    /**
     * 筛选服务端连接
     *
     * @param invocation
     *         请求
     * @param providers
     *         可用连接
     * @return provider
     */
    public Endpoint select( List<Endpoint> providers) {
        if (providers.size() == 0) {
            throw new NoAliveEndpointException(consumerConfig.getAppName(), providers);
        }
        if (providers.size() == 1) {
            return providers.get(0);
        } else {
            return doSelect(providers);
        }
    }

    /**
     * 根据负载均衡筛选
     *
     * @param invocation
     *         请求
     * @param providers
     *         全部服务端连接
     * @return 服务端连接 provider
     */

    public abstract Endpoint doSelect( List<Endpoint> providers);

    /**
     * Gets consumer config.
     *
     * @return the consumer config
     */
    public ClientEndpointConfig getConsumerConfig() {
        return consumerConfig;
    }

    /**
     * Sets consumer config.
     *
     * @param consumerConfig
     *         the consumer config
     */
    public void setConsumerConfig(ClientEndpointConfig consumerConfig) {
        this.consumerConfig = consumerConfig;
    }

    /**
     * Gets weight.
     *
     * @param provider
     *         the provider
     * @return the weight
     */
    protected int getWeight(Endpoint provider) {
        // 从provider中或得到相关权重,默认值100
        return provider.getWeight() < 0 ? 0 : provider.getWeight();
    }
}