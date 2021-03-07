package stubbornwdb.werpc.transport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stubbornwdb.werpc.entity.WeRpcRequest;
import stubbornwdb.werpc.entity.WeRpcResponse;
import stubbornwdb.werpc.transport.netty.client.WeRpcNettyClient;
import stubbornwdb.werpc.transport.socket.client.WeRpcSocketClient;
import stubbornwdb.werpc.util.WeRpcMessageChecker;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * RPC客户端动态代理
 */
public class WeRpcClientProxy implements InvocationHandler {

    private static final Logger logger = LoggerFactory.getLogger(WeRpcClientProxy.class);

    private final WeRpcClient client;

    public WeRpcClientProxy(WeRpcClient client) {
        this.client = client;
    }

    @SuppressWarnings("unchecked")
    public <T> T getProxy(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, this);
    }

    /**
     * 动态代理对象调用方法的时候将执行invoke方法
     * @param proxy
     * @param method
     * @param args
     * @return
     */
    @SuppressWarnings("unchecked")
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        logger.info("调用方法: {}#{}", method.getDeclaringClass().getName(), method.getName());
        WeRpcRequest weRpcRequest = new WeRpcRequest(UUID.randomUUID().toString(), method.getDeclaringClass().getName(),
                method.getName(), args, method.getParameterTypes(), false);
        WeRpcResponse weRpcResponse = null;
        if (client instanceof WeRpcNettyClient) {
            try {
                CompletableFuture<WeRpcResponse> completableFuture = (CompletableFuture<WeRpcResponse>) client.sendRequest(weRpcRequest);
                weRpcResponse = completableFuture.get();
            } catch (Exception e) {
                logger.error("方法调用请求发送失败", e);
                return null;
            }
        }
        if (client instanceof WeRpcSocketClient) {
            weRpcResponse = (WeRpcResponse) client.sendRequest(weRpcRequest);
        }
        WeRpcMessageChecker.check(weRpcRequest, weRpcResponse);
        return weRpcResponse.getData();
    }
}
