package stubbornwdb.werpc.proxy;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import stubbornwdb.werpc.entity.WeRpcServiceProperties;
import stubbornwdb.werpc.enums.WeRpcErrorMessageEnum;
import stubbornwdb.werpc.enums.WeRpcResponseCodeEnum;
import stubbornwdb.werpc.exception.WeRpcException;
import stubbornwdb.werpc.remoting.dto.WeRpcRequest;
import stubbornwdb.werpc.remoting.dto.WeRpcResponse;
import stubbornwdb.werpc.remoting.transport.WeRpcRequestTransport;
import stubbornwdb.werpc.remoting.transport.netty.client.NettyWeRpcClient;
import stubbornwdb.werpc.remoting.transport.socket.SocketWeRpcClient;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Dynamic proxy class.
 * When a dynamic proxy object calls a method, it actually calls the following invoke method.
 * It is precisely because of the dynamic proxy that the remote method called by the client is like calling the local method (the intermediate process is shielded)
 *
 */
@Slf4j
public class RpcClientProxy implements InvocationHandler {

    private static final String INTERFACE_NAME = "interfaceName";

    /**
     * Used to send requests to the server.And there are two implementations: socket and netty
     */
    private final WeRpcRequestTransport rpcRequestTransport;
    private final WeRpcServiceProperties weRpcServiceProperties;

    public RpcClientProxy(WeRpcRequestTransport rpcRequestTransport, WeRpcServiceProperties weRpcServiceProperties) {
        this.rpcRequestTransport = rpcRequestTransport;
        if (weRpcServiceProperties.getGroup() == null) {
            weRpcServiceProperties.setGroup("");
        }
        if (weRpcServiceProperties.getVersion() == null) {
            weRpcServiceProperties.setVersion("");
        }
        this.weRpcServiceProperties = weRpcServiceProperties;
    }


    public RpcClientProxy(WeRpcRequestTransport rpcRequestTransport) {
        this.rpcRequestTransport = rpcRequestTransport;
        this.weRpcServiceProperties = WeRpcServiceProperties.builder().group("").version("").build();
    }

    /**
     * get the proxy object
     */
    @SuppressWarnings("unchecked")
    public <T> T getProxy(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, this);
    }

    /**
     * This method is actually called when you use a proxy object to call a method.
     * The proxy object is the object you get through the getProxy method.
     */
    @SneakyThrows
    @SuppressWarnings("unchecked")
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        log.info("invoked method: [{}]", method.getName());
        WeRpcRequest weRpcRequest = WeRpcRequest.builder().methodName(method.getName())
                .parameters(args)
                .interfaceName(method.getDeclaringClass().getName())
                .paramTypes(method.getParameterTypes())
                .requestId(UUID.randomUUID().toString())
                .group(weRpcServiceProperties.getGroup())
                .version(weRpcServiceProperties.getVersion())
                .build();
        WeRpcResponse<Object> weRpcResponse = null;
        if (rpcRequestTransport instanceof NettyWeRpcClient) {
            CompletableFuture<WeRpcResponse<Object>> completableFuture = (CompletableFuture<WeRpcResponse<Object>>) rpcRequestTransport.sendRpcRequest(weRpcRequest);
            weRpcResponse = completableFuture.get();
        }
        if (rpcRequestTransport instanceof SocketWeRpcClient) {
            weRpcResponse = (WeRpcResponse<Object>) rpcRequestTransport.sendRpcRequest(weRpcRequest);
        }
        this.check(weRpcResponse, weRpcRequest);
        return weRpcResponse.getData();
    }

    private void check(WeRpcResponse<Object> weRpcResponse, WeRpcRequest weRpcRequest) {
        if (weRpcResponse == null) {
            throw new WeRpcException(WeRpcErrorMessageEnum.SERVICE_INVOCATION_FAILURE, INTERFACE_NAME + ":" + weRpcRequest.getInterfaceName());
        }

        if (!weRpcRequest.getRequestId().equals(weRpcResponse.getRequestId())) {
            throw new WeRpcException(WeRpcErrorMessageEnum.REQUEST_NOT_MATCH_RESPONSE, INTERFACE_NAME + ":" + weRpcRequest.getInterfaceName());
        }

        if (weRpcResponse.getCode() == null || !weRpcResponse.getCode().equals(WeRpcResponseCodeEnum.SUCCESS.getCode())) {
            throw new WeRpcException(WeRpcErrorMessageEnum.SERVICE_INVOCATION_FAILURE, INTERFACE_NAME + ":" + weRpcRequest.getInterfaceName());
        }
    }
}
