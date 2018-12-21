package com.syj.tcpentrypoint.client;

import java.util.List;
import java.util.Random;


/**
 * Title: 负载均衡随机算法<br>
 * <p/>
 * Description: 全部列表按权重随机选择<br>
 * <p/>
 */
public class RandomLoadbalance extends Loadbalance {

    /**
     * 随机
     */
    private final Random random = new Random();

    /**
     * @see Loadbalance#doSelect(Invocation, java.util.List)
     */
    public Endpoint doSelect( List<Endpoint> providers) {

        Endpoint provider = null;
        int length = providers.size(); // 总个数
        int totalWeight = 0; // 总权重
        boolean sameWeight = true; // 权重是否都一样
        for (int i = 0; i < length; i++) {
            int weight = getWeight(providers.get(i));
            totalWeight += weight; // 累计总权重
            if (sameWeight && i > 0
                    && weight != getWeight(providers.get(i - 1))) {
                sameWeight = false; // 计算所有权重是否一样
            }
        }
        if (totalWeight > 0 && !sameWeight) {
            // 如果权重不相同且权重大于0则按总权重数随机
            int offset = random.nextInt(totalWeight);
            // 并确定随机值落在哪个片断上
            for (int i = 0; i < length; i++) {
                offset -= getWeight(providers.get(i));
                if (offset < 0) {
                    provider = providers.get(i);
                    break;
                }
            }
        } else {
            // 如果权重相同或权重为0则均等随机
            provider = providers.get(random.nextInt(length));
        }
        return provider;
    }


}