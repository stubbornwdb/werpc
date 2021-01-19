package test;

import stubbornwdb.werpc.annotation.WeRpcServiceScan;
import stubbornwdb.werpc.serializer.CommonSerializer;
import stubbornwdb.werpc.transport.WeRpcServer;
import stubbornwdb.werpc.transport.socket.server.WeRpcSocketServer;

/**
 * 测试用服务提供方（服务端）
 */
@WeRpcServiceScan
public class SocketTestServer {

    public static void main(String[] args) {
        WeRpcServer server = new WeRpcSocketServer("127.0.0.1", 9998, CommonSerializer.SMARTBUF_SERIALIZER);
        server.start();
    }

}
