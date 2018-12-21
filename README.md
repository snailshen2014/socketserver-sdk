1.介绍
  基于netty4的tcp通讯框架，包括client,server端，方便有需要的拿来学习netty4，client
  端向server端发送消息，只需要通过api调用即可。为实现rpc框架提供了基本的通讯参考

2.特性
  基于nett4开发的分布式socker server
  支持自定义协议
  client端库，支持对多个server端点的loadblance
  长连接，支持连接失效重连、心跳包检测等机制
  
3.远期规划
  支持client异步调用server端
  负载均衡，支持其他算法
  包的序列化，反序列化支撑
  支持其它协议，如pb
  基于这个sdk实现rpc框架
  
  
