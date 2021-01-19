# werpc   微RPC

## 介绍
A simple rpc framework —— werpc.

RPC(Remote Procedure Call):远程过程调用，像调用本地方法一样调用远程过程。采用Client-Server结构，通过request-response消息模式实现。  
RPC框架就是指封装好了参数编组、消息解组、底层网络通信的RPC程序开发框架，可以在这个框架的基础上只需专注与业务代码的编写。
常见rpc实现:  
- RMI,Remote method invocation,远程方法调用是oop领域中rpc的一种具体实现  
- webservice + xml  
- restful接口调用(http+json)  
- ...
  
## RPC流程
1. 客户端处理过程中调用Client stub(就像代用本地方法一样)，传入参数；
2. Client stub 将参数编织为消息，通过网络向服务端发送消息；
3. 服务端Server stub 接收请求消息，解组消息为参数；
4. Server stub再调用服务端的过程，过程执行结果以反方向的相同步骤响应给客户端

## RPC结构
![](./images/werpc-architecture.png)
- **注册中心**  
服务注册与发现，可以使用zookeeper、nacos等作为服务的注册中心，服务端向注册中心注册服务，客户端从服务端发现服务(获取服务信息，如服务器地址等)。
- **服务端**  
提供服务，有服务接口的具体实现，使用反射调用具体实现(stub)
- **客户端**  
获取服务，实现负载均衡策略，调用服务，通过动态代理的方式将网络传输细节封装起来(stub)。
- **序列化与反序列化**  
网络传输必然离不开序列化与反序列化，常见的序列化方式有：XML、JSON、Protobuf、Thrift、hessian、kryo、Avro等  

## 使用
**默认使用Nacos作为注册中心，所以使用是需确保Nacos已经启动，并且端口为8848**

1.服务端    
引入werpc-api、 werpc-api模块    
初始化Server(可以使用SocketServer 或 NettyServer),设置Server初始化参数      
实现公共接口，并且使用@WeRpcService注解实现类     

2.客户端  
引入werpc-api、 werpc-api模块   
创建RPC客户端、动态代理器  
动态代理对象调用服务  

先启动服务端，在启动客户端
具体见werpc-test-server、 werpc-test-client
