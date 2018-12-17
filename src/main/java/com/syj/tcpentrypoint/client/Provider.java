package com.syj.tcpentrypoint.client;

import java.io.Serializable;

import com.syj.tcpentrypoint.util.Constants;

/**
 * 
 * @des :代表一个server
 * @author:shenyanjun1
 * @date :2018-12-17 10:22
 */
public class Provider implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1191734773056283936L;

	/**
	 * The Ip.
	 */
	private String ip;

	/**
	 * The Port.
	 */
	private int port;
	/**
	 * The Weight.
	 */
	private int weight = Constants.DEFAULT_PROVIDER_WEIGHT;
	/**
	 * 重连周期系数：1-5（即5次才真正调一次）
	 */
	private transient int reconnectPeriodCoefficient = 1;

	/**
	 * Instantiates a new Provider.
	 */
	public Provider() {

	}

	/**
	 * Instantiates a new Provider.
	 *
	 * @param host the host
	 * @param port the port
	 */
	private Provider(String host, int port) {
		this.ip = host;
		this.port = port;
	}

	/**
	 * Get provider.
	 *
	 * @param host the host
	 * @param port the port
	 * @return the provider
	 */
	public static Provider getProvider(String host, int port) {
		return new Provider(host, port);
	}

	/**
	 * Sets ip.
	 *
	 * @param ip the ip
	 */
	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getIp() {
		return this.ip;
	}

	/**
	 * Gets port.
	 *
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Sets port.
	 *
	 * @param port the port
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * Equals boolean.
	 *
	 * @param o the o
	 * @return the boolean
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof Provider))
			return false;

		Provider provider = (Provider) o;
		if (port != provider.port)
			return false;
		if (ip != null ? !ip.equals(provider.ip) : provider.ip != null)
			return false;
		if (weight != provider.weight)
			return false;

		return true;
	}

	/**
	 * Hash code.
	 *
	 * @return the int
	 */
	@Override
	public int hashCode() {
		int result = ip != null ? ip.hashCode() : 0;
		result = 31 * result + port;
		result = 31 * result + weight;
		return result;
	}

	/**
	 * Gets weight.
	 *
	 * @return the weight
	 */
	public int getWeight() {
		return weight;
	}

	/**
	 * Sets weight.
	 *
	 * @param weight the weight to set
	 */
	public void setWeight(int weight) {
		this.weight = weight;
	}

	/**
	 * Gets reconnect period coefficient.
	 *
	 * @return the reconnect period coefficient
	 */
	public int getReconnectPeriodCoefficient() {
		// 最大是5
		reconnectPeriodCoefficient = Math.min(5, reconnectPeriodCoefficient);
		return reconnectPeriodCoefficient;
	}

	/**
	 * Sets reconnect period coefficient.
	 *
	 * @param reconnectPeriodCoefficient the reconnect period coefficient
	 */
	public void setReconnectPeriodCoefficient(int reconnectPeriodCoefficient) {
		// 最小是1
		reconnectPeriodCoefficient = Math.max(1, reconnectPeriodCoefficient);
		this.reconnectPeriodCoefficient = reconnectPeriodCoefficient;
	}
}