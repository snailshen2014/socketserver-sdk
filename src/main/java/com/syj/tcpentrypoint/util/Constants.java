package com.syj.tcpentrypoint.util;

import java.nio.charset.Charset;

/**
 * Title:常量类<br>
 * <p/>
 * Description: 各种常量和枚举<br>
 * <p/>
 */
public class Constants {
    /**
     * RE协议头的魔术位,只用一位
     */
    public static final byte MAGICCODEBYTE = (byte) 0xFF;

    //RE protocol MESSAGETYPE+MSGID size,if > 5 then contains the map attr 
    public static final int REPROTOCOL_FRAME_NOMAP_SIZE=5;
    /*---------消息类型开始-----------*/
    public static final int REQUEST_MSG = 1;
    public static final int RESPONSE_MSG = 2;
    public static final int HEARTBEAT_REQUEST_MSG = 10;//connection heartbeat message
    public static final int HEARTBEAT_RESPONSE_MSG = 11;//connection heartbeat message

    /*---------消息类型结束-----------*/



    public static final Boolean isLinux = true;

    public static final int CPU_CORES = 4;
    /*---------环境变量结束-----------*/

    /**
     * 默认权重 100
     */
    public final static int DEFAULT_PROVIDER_WEIGHT = 100;
    /**
     * 随机
     */
    public static final String LOADBALANCE_RANDOM = "random";

    /**
     * 轮询
     */
    public static final String LOADBALANCE_ROUNDROBIN = "roundrobin";

    /**
     * 最小调用
     */
    public static final String LOADBALANCE_LEASTACTIVE = "leastactive";



    /**
     * 分发全部
     */
    public static final String CLUSTER_BROADCAST = "broadcast";

   


    /**
     * 默认consumer连provider超时时间
     */
    public static final int DEFAULT_CLIENT_CONNECT_TIMEOUT = 5000;

    /**
     * 默认consumer断开时等待结果的超时时间
     */
    public static final int DEFAULT_CLIENT_DISCONNECT_TIMEOUT = 10000;

    /**
     * 默认consumer调用provider超时时间
     */
    public static final int DEFAULT_CLIENT_INVOKE_TIMEOUT = 5000;

    /**
     * 客户端channelhandler名字
     */
    public static final String CLIENT_CHANNELHANDLE_NAME = "RE_CLIENT_CHANNELHANDLE";
    
    /**
     * 线程池类型：固定线程池
     */
    public final static String THREADPOOL_TYPE_FIXED = "fixed";

    /**
     * 线程池类型：伸缩线程池
     */
    public final static String THREADPOOL_TYPE_CACHED = "cached";

    /**
     * 事件分发类型：all 所有消息都派发到业务线程池，包括请求，响应，连接事件，断开事件，心跳等。
     */
    public final static String DISPATCHER_ALL = "all";

    /**
     * 事件分发类型：direct 所有消息都不派发到线程池，全部在IO线程上直接执行。
     */
    public final static String DISPATCHER_DIRECT = "direct";

    /**
     * 事件分发类型：message 只有请求响应消息派发到线程池，其它连接断开事件，心跳等消息，直接在IO线程上执行。
     */
    public final static String DISPATCHER_MESSAGE = "message";

    /**
     * 事件分发类型：execution 只请求消息派发到线程池，不含响应，响应和其它连接断开事件，心跳等消息，直接在IO线程上执行。
     */
    public final static String DISPATCHER_EXECUTION = "execution";

    /**
     * 事件分发类型：connection 在IO线程上，将连接断开事件放入队列，有序逐个执行，其它消息派发到线程池。
     */
    public final static String DISPATCHER_CONNECTION = "connection";

    /**
     * 队列类型：普通队列
     */
    public final static String QUEUE_TYPE_NORMAL = "normal";

    /**
     * 队列类型：优先级队列
     */
    public final static String QUEUE_TYPE_PRIORITY = "priority";

    /**
     * 默认io线程池大小
     */
    public final static int DEFAULT_IO_THREADS = CPU_CORES + 1;

    /**
     * 默认服务端业务线程池大小
     */
    public final static int DEFAULT_SERVER_BIZ_THREADS = 200;

    /**
     * 默认服务端业务线程池队列大小
     */
    public final static int DEFAULT_SERVER_QUEUE = 0;

    /**
     * 默认客户端线程池大小
     */
    public final static int DEFAULT_CLIENT_BIZ_THREADS = 20;

    /**
     * 默认客户端异步返回调用线程池大小
     */
    public final static int DEFAULT_CLIENT_CALLBACK_CORE_THREADS = 20;

    /**
     * 默认客户端异步返回调用线程池大小
     */
    public final static int DEFAULT_CLIENT_CALLBACK_MAX_THREADS = 200;

    /**
     * 默认客户端异步返回调用线程池队列大小
     */
    public final static int DEFAULT_CLIENT_CALLBACK_QUEUE = 256;

    /**
     * 默认协议类型:rulesengine protocol
     */
    public final static ProtocolType DEFAULT_PROTOCOL_TYPE = ProtocolType.re;

    /**
     * 默认协议:re
     */
    public final static String DEFAULT_PROTOCOL = "re";

    /**
     * 默认字符集 utf-8
     */
    public final static Charset DEFAULT_CHARSET = Charset.forName("UTF-8");


    /**
     * 默认服务端 数据包限制
     */
    public final static int DEFAULT_PAYLOAD = 8 * 1024 * 1024;
    /**
     * 默认IO的buffer大小
     */
    public final static int DEFAULT_BUFFER_SIZE = 8 * 1024;
    /**
     * 最大IO的buffer大小
     */
    public final static int MAX_BUFFER_SIZE = 32 * 1024;
    /**
     * 最小IO的buffer大小
     */
    public final static int MIN_BUFFER_SIZE = 1 * 1024;

    /**
     * 发心跳的间隔
     */
    public final static int DEFAULT_HEARTBEAT_TIME = 30000;

    /**
     * 重连的间隔
     */
    public final static int DEFAULT_RECONNECT_TIME = 10000;

    /**--------Config配置值相关结束---------*/




    /**--------上下文KEY相关开始---------*/
    /**
     * 内部使用的key前缀，防止和自定义key冲突
     */
    public static final char INTERNAL_KEY_PREFIX = '_';
    /**
     * 内部使用的key：请求是否keep-alive
     *
     * @since 1.6.0
     */
    public static final String INTERNAL_KEY_KEEPALIVE = INTERNAL_KEY_PREFIX + "keepAlive";

    /**
     * 隐藏的key前缀，隐藏的key只能在filter里拿到，在RpcContext里拿不到，不过可以设置
     */
    public static final char HIDE_KEY_PREFIX = '.';
    /**
     * 隐藏属性的key：token
     */
    public static final String HIDDEN_KEY_TOKEN = HIDE_KEY_PREFIX + "token";
    /**
     * 隐藏属性的key：monitor是否开启
     */
    public static final String HIDDEN_KEY_MONITOR = HIDE_KEY_PREFIX + "monitor";
    /**
     * 隐藏属性的key：指定远程调用地址
     */
    public static final String HIDDEN_KEY_PINPOINT = HIDE_KEY_PREFIX + "pinpoint";
    /**
     * 隐藏属性的key：consumer发布是否警告检查
     */
    public static final String HIDDEN_KEY_WARNNING = HIDE_KEY_PREFIX + "warnning";
    /**
     * 隐藏属性的key：consumer是否自动销毁（例如Registry和Monitor不需要自动销毁）
     */
    public static final String HIDDEN_KEY_DESTROY = HIDE_KEY_PREFIX + "destroy";
    /**
     * 隐藏属性的key：自动部署appId
     */
    public static final String HIDDEN_KEY_APPID = HIDE_KEY_PREFIX + "appId";
    /**
     * 隐藏属性的key：自动部署appName
     */
    public static final String HIDDEN_KEY_APPNAME = HIDE_KEY_PREFIX + "appName";
    /**
     * 隐藏属性的key：自动部署实例Id
     */
    public static final String HIDDEN_KEY_APPINSID = HIDE_KEY_PREFIX + "appInsId";
    /**
     * 隐藏属性的key：session
     */
    public static final String HIDDEN_KEY_SESSION = HIDE_KEY_PREFIX + "session";
	
    /**--------上下文KEY相关结束---------*/

    /**--------配置项相关开始---------*/
    /**
     * 配置key:generic
     */
    public static final String CONFIG_KEY_GENERIC = "generic";
    /**
     * 配置key:async
     */
    public static final String CONFIG_KEY_ASYNC = "async";
    /**
     * 配置key:retries
     */
    public static final String CONFIG_KEY_RETRIES = "retries";

    /**
     * 配置key:timeout
     */
    public static final String CONFIG_KEY_TIMEOUT = "timeout";

    /**
     * 配置key:concurrents
     */
    public static final String CONFIG_KEY_CONCURRENTS = "concurrents";

    /**
     * 配置key:params
     */
    public static final String CONFIG_KEY_PARAMS = "parameters";

    /**
     * 配置key:onreturn
     */
    public static final String CONFIG_KEY_ONRETURN = "onreturn";

    /**
     * 配置key:weight
     */
    public static final String CONFIG_KEY_WEIGHT = "weight";

    /**
     * 配置key:safVersion
     */
    public static final String CONFIG_KEY_SAFVERSION = "safVersion";

    /**
     * 配置key:interface | interfaceId
     */
    public static final String CONFIG_KEY_INTERFACE = "interface";

    /**
     * 配置key:alias
     */
    public static final String CONFIG_KEY_ALIAS = "alias";

    /**
     * 配置key:dynamic
     */
    public static final String CONFIG_KEY_DYNAMIC = "dynamic";

    /**
     * 配置key:validation
     */
    public static final String CONFIG_KEY_VALIDATION = "validation";

    /**
     * 配置key:mock
     */
    public static final String CONFIG_KEY_MOCK = "mock";

    /**
     * 配置key:cache
     */
    public static final String CONFIG_KEY_CACHE = "cache";




    

    /**--------系统参数相关结束---------*/

    /**
     * 服务协议
     */
    public enum ProtocolType {

        re(1),
        rest(2),
        dubbo(3),
        webservice(4),
        jaxws(5),
        @Deprecated jaxrs(6),
        @Deprecated hessian(7),
        @Deprecated thrift(8),
        http(9);

        private int value;

        private ProtocolType(int mvalue) {
            this.value = mvalue;
        }

        public int value() {
            return value;
        }

        public static ProtocolType valueOf(int value) {
            ProtocolType p;
            switch (value) {
                case 1:
                    p = re;
                    break;
                case 2:
                    p = rest;
                    break;
                case 9:
                    p = http;
                    break;
                case 3:
                    p = dubbo;
                    break;
                case 4:
                    p = webservice;
                    break;
                case 5:
                    p = jaxws;
                    break;
                case 6:
                    p = jaxrs;
                    break;
                case 7:
                    p = hessian;
                    break;
                case 8:
                    p = thrift;
                    break;
                default:
                    throw new IllegalArgumentException("Unknown protocol type value: " + value);
            }
            return p;
        }
    }

    /**
     * 序列化方式类型,兼容1.x版本
     */
    public enum CodecType {

        @Deprecated dubbo(1),
        hessian(2),
        java(3),
        @Deprecated compactedjava(4),
        json(5),
        @Deprecated fastjson(6),
        @Deprecated nativejava(7),
        @Deprecated kryo(8),
        msgpack(10),
        @Deprecated nativemsgpack(11),
        protobuf(12);

        private int value;

        private CodecType(int mvalue) {
            this.value = mvalue;
        }

        public int value() {
            return value;
        }

        public static CodecType valueOf(int value) {
            CodecType p;
            switch (value) {
                case 10:
                    p = msgpack;
                    break;
                case 2:
                    p = hessian;
                    break;
                case 3:
                    p = java;
                    break;
                case 12:
                    p = protobuf;
                    break;
                case 5:
                    p = json;
                    break;
                case 1:
                    p = dubbo;
                    break;
                case 11:
                    p = nativemsgpack;
                    break;
                case 4:
                    p = compactedjava;
                    break;
                case 6:
                    p = fastjson;
                    break;
                case 7:
                    p = nativejava;
                    break;
                case 8:
                    p = kryo;
                    break;
                default:
                    throw new IllegalArgumentException("Unknown codec type value: " + value);
            }
            return p;
        }
    }

    /**
     * enum for head key number
     */
    public enum HeadKey {
        topicid((byte) 1, String.class), //设备id
        productkey((byte) 2, String.class);// 产品key
    

        private byte keyNum;
        private Class type;

        private HeadKey(byte b, Class clazz) {
            this.keyNum = b;
            this.type = clazz;
        }

        public byte getNum() {
            return this.keyNum;
        }

        public Class getType() {
            return this.type;
        }

        public static HeadKey getKey(byte num) {
            HeadKey key = null;
            switch (num) {
                case 1:
                    key = topicid;
                    break;
                case 2:
                    key = productkey;
                    break;
                default:
                    throw new IllegalArgumentException("Unknown head key value: " + num);
            }
            return key;

        }

    }
 
}