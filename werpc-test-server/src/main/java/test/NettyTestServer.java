package test;


import stubbornwdb.werpc.annotation.WeRpcServiceScan;
import stubbornwdb.werpc.serializer.CommonSerializer;
import stubbornwdb.werpc.transport.WeRpcServer;
import stubbornwdb.werpc.transport.netty.server.WeRpcNettyServer;

/**
 * 测试用Netty服务提供者（服务端）
 */
@WeRpcServiceScan
public class NettyTestServer {

    public static void main(String[] args) {
        WeRpcServer server = new WeRpcNettyServer("127.0.0.1", 9999, CommonSerializer.PROTOBUF_SERIALIZER);
        server.start();
    }

}
