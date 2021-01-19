package test;


import stubbornwdb.werpc.api.ByeService;
import stubbornwdb.werpc.api.HelloObject;
import stubbornwdb.werpc.api.HelloService;
import stubbornwdb.werpc.serializer.CommonSerializer;
import stubbornwdb.werpc.transport.WeRpcClient;
import stubbornwdb.werpc.transport.WeRpcClientProxy;
import stubbornwdb.werpc.transport.netty.client.WeRpcNettyClient;

/**
 * 测试用Netty消费者
 */
public class NettyTestClient {

    public static void main(String[] args) {
        //创建RPC客户端
        WeRpcClient client = new WeRpcNettyClient(CommonSerializer.SMARTBUF_SERIALIZER);
        //动态代理器，将客户端传入动态代理器
        WeRpcClientProxy rpcClientProxy = new WeRpcClientProxy(client);
        //获取动态代理对象
        HelloService helloService = rpcClientProxy.getProxy(HelloService.class);

        //使用动态代理对象调用服务
        HelloObject object = new HelloObject(12, "This is a message");
        String res = helloService.hello(object);
        System.out.println(res);

        ByeService byeService = rpcClientProxy.getProxy(ByeService.class);
        System.out.println(byeService.bye("Netty"));
    }

}
