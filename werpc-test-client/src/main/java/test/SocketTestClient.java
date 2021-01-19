package test;

import stubbornwdb.werpc.api.ByeService;
import stubbornwdb.werpc.api.HelloObject;
import stubbornwdb.werpc.api.HelloService;
import stubbornwdb.werpc.serializer.CommonSerializer;
import stubbornwdb.werpc.transport.WeRpcClientProxy;
import stubbornwdb.werpc.transport.socket.client.WeRpcSocketClient;

/**
 * 测试用消费者（客户端）
 */
public class SocketTestClient {

    public static void main(String[] args) {
        WeRpcSocketClient client = new WeRpcSocketClient(CommonSerializer.SMARTBUF_SERIALIZER);
        WeRpcClientProxy proxy = new WeRpcClientProxy(client);
        HelloService helloService = proxy.getProxy(HelloService.class);
        HelloObject object = new HelloObject(12, "This is a message");
        String res = helloService.hello(object);
        System.out.println(res);
        ByeService byeService = proxy.getProxy(ByeService.class);
        System.out.println(byeService.bye("Netty"));
    }

}
