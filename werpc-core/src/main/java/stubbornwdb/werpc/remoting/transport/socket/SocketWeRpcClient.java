package stubbornwdb.werpc.remoting.transport.socket;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import stubbornwdb.werpc.entity.WeRpcServiceProperties;
import stubbornwdb.werpc.exception.WeRpcException;
import stubbornwdb.werpc.extension.ExtensionLoader;
import stubbornwdb.werpc.registry.ServiceDiscovery;
import stubbornwdb.werpc.remoting.dto.WeRpcRequest;
import stubbornwdb.werpc.remoting.transport.WeRpcRequestTransport;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * 基于 Socket 传输 RpcRequest
 *
 */
@AllArgsConstructor
@Slf4j
public class SocketWeRpcClient implements WeRpcRequestTransport {
    private final ServiceDiscovery serviceDiscovery;

    public SocketWeRpcClient() {
        this.serviceDiscovery = ExtensionLoader.getExtensionLoader(ServiceDiscovery.class).getExtension("zk");
    }

    @Override
    public Object sendRpcRequest(WeRpcRequest weRpcRequest) {
        // build rpc service name by rpcRequest
        String rpcServiceName = WeRpcServiceProperties.builder().serviceName(weRpcRequest.getInterfaceName())
                .group(weRpcRequest.getGroup()).version(weRpcRequest.getVersion()).build().toRpcServiceName();
        InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcServiceName);
        try (Socket socket = new Socket()) {
            socket.connect(inetSocketAddress);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            // Send data to the server through the output stream
            objectOutputStream.writeObject(weRpcRequest);
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            // Read RpcResponse from the input stream
            return objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new WeRpcException("调用服务失败:", e);
        }
    }
}
