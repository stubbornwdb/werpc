package stubbornwdb.werpc.remoting.transport.socket;

import lombok.extern.slf4j.Slf4j;
import stubbornwdb.werpc.config.CustomShutdownHook;
import stubbornwdb.werpc.entity.WeRpcServiceProperties;
import stubbornwdb.werpc.factory.SingletonFactory;
import stubbornwdb.werpc.provider.ServiceProvider;
import stubbornwdb.werpc.provider.ServiceProviderImpl;
import stubbornwdb.werpc.utils.concurrent.threadpool.ThreadPoolFactoryUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

import static stubbornwdb.werpc.remoting.transport.netty.server.NettyRpcServer.PORT;


@Slf4j
public class SocketRpcServer {

    private final ExecutorService threadPool;
    private final ServiceProvider serviceProvider;


    public SocketRpcServer() {
        threadPool = ThreadPoolFactoryUtils.createCustomThreadPoolIfAbsent("socket-server-rpc-pool");
        serviceProvider = SingletonFactory.getInstance(ServiceProviderImpl.class);
    }

    public void registerService(Object service) {
        serviceProvider.publishService(service);
    }

    public void registerService(Object service, WeRpcServiceProperties weRpcServiceProperties) {
        serviceProvider.publishService(service, weRpcServiceProperties);
    }

    public void start() {
        try (ServerSocket server = new ServerSocket()) {
            String host = InetAddress.getLocalHost().getHostAddress();
            server.bind(new InetSocketAddress(host, PORT));
            CustomShutdownHook.getCustomShutdownHook().clearAll();
            Socket socket;
            while ((socket = server.accept()) != null) {
                log.info("client connected [{}]", socket.getInetAddress());
                threadPool.execute(new SocketRpcRequestHandlerRunnable(socket));
            }
            threadPool.shutdown();
        } catch (IOException e) {
            log.error("occur IOException:", e);
        }
    }

}
