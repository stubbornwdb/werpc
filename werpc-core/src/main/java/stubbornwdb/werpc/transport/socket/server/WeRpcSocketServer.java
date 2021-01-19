package stubbornwdb.werpc.transport.socket.server;


import stubbornwdb.werpc.factory.ThreadPoolFactory;
import stubbornwdb.werpc.handler.RequestHandler;
import stubbornwdb.werpc.hook.ShutdownHook;
import stubbornwdb.werpc.provider.ServiceProviderImpl;
import stubbornwdb.werpc.registry.NacosServiceRegistry;
import stubbornwdb.werpc.serializer.CommonSerializer;
import stubbornwdb.werpc.transport.AbstractWeRpcServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

/**
 * Socket方式远程方法调用的提供者（服务端）
 */
public class WeRpcSocketServer extends AbstractWeRpcServer {

    private final ExecutorService threadPool;
    private final CommonSerializer serializer;
    private final RequestHandler requestHandler = new RequestHandler();

    public WeRpcSocketServer(String host, int port) {
        this(host, port, DEFAULT_SERIALIZER);
    }

    public WeRpcSocketServer(String host, int port, Integer serializer) {
        this.host = host;
        this.port = port;
        threadPool = ThreadPoolFactory.createDefaultThreadPool("socket-rpc-server");
        this.serviceRegistry = new NacosServiceRegistry();
        this.serviceProvider = new ServiceProviderImpl();
        this.serializer = CommonSerializer.getByCode(serializer);
        scanServices();
    }

    @Override
    public void start() {
        try (ServerSocket serverSocket = new ServerSocket()) {
            serverSocket.bind(new InetSocketAddress(host, port));
            logger.info("服务器启动……");
            ShutdownHook.getShutdownHook().addClearAllHook();
            Socket socket;
            while ((socket = serverSocket.accept()) != null) {
                logger.info("消费者连接: {}:{}", socket.getInetAddress(), socket.getPort());
                threadPool.execute(new SocketRequestHandlerThread(socket, requestHandler, serializer));
            }
            threadPool.shutdown();
        } catch (IOException e) {
            logger.error("服务器启动时有错误发生:", e);
        }
    }

}
