package stubbornwdb.werpc.transport.socket.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stubbornwdb.werpc.entity.WeRpcRequest;
import stubbornwdb.werpc.entity.WeRpcResponse;
import stubbornwdb.werpc.enumeration.ResponseCode;
import stubbornwdb.werpc.enumeration.WeRpcError;
import stubbornwdb.werpc.exception.WeRpcException;
import stubbornwdb.werpc.loadbalancer.LoadBalancer;
import stubbornwdb.werpc.loadbalancer.RandomLoadBalancer;
import stubbornwdb.werpc.registry.NacosServiceDiscovery;
import stubbornwdb.werpc.registry.ServiceDiscovery;
import stubbornwdb.werpc.serializer.CommonSerializer;
import stubbornwdb.werpc.transport.WeRpcClient;
import stubbornwdb.werpc.transport.socket.util.ObjectReader;
import stubbornwdb.werpc.transport.socket.util.ObjectWriter;
import stubbornwdb.werpc.util.WeRpcMessageChecker;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Socket方式远程方法调用的消费者（客户端）
 */
public class WeRpcSocketClient implements WeRpcClient {

    private static final Logger logger = LoggerFactory.getLogger(WeRpcSocketClient.class);

    private final ServiceDiscovery serviceDiscovery;

    private final CommonSerializer serializer;

    public WeRpcSocketClient() {
        this(DEFAULT_SERIALIZER, new RandomLoadBalancer());
    }
    public WeRpcSocketClient(LoadBalancer loadBalancer) {
        this(DEFAULT_SERIALIZER, loadBalancer);
    }
    public WeRpcSocketClient(Integer serializer) {
        this(serializer, new RandomLoadBalancer());
    }

    public WeRpcSocketClient(Integer serializer, LoadBalancer loadBalancer) {
        this.serviceDiscovery = new NacosServiceDiscovery(loadBalancer);
        this.serializer = CommonSerializer.getByCode(serializer);
    }

    @Override
    public Object sendRequest(WeRpcRequest weRpcRequest) {
        if(serializer == null) {
            logger.error("未设置序列化器");
            throw new WeRpcException(WeRpcError.SERIALIZER_NOT_FOUND);
        }
        InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(weRpcRequest.getInterfaceName());
        try (Socket socket = new Socket()) {
            socket.connect(inetSocketAddress);
            OutputStream outputStream = socket.getOutputStream();
            InputStream inputStream = socket.getInputStream();
            ObjectWriter.writeObject(outputStream, weRpcRequest, serializer);
            Object obj = ObjectReader.readObject(inputStream);
            WeRpcResponse weRpcResponse = (WeRpcResponse) obj;
            if (weRpcResponse == null) {
                logger.error("服务调用失败，service：{}", weRpcRequest.getInterfaceName());
                throw new WeRpcException(WeRpcError.SERVICE_INVOCATION_FAILURE, " service:" + weRpcRequest.getInterfaceName());
            }
            if (weRpcResponse.getStatusCode() == null || weRpcResponse.getStatusCode() != ResponseCode.SUCCESS.getCode()) {
                logger.error("调用服务失败, service: {}, response:{}", weRpcRequest.getInterfaceName(), weRpcResponse);
                throw new WeRpcException(WeRpcError.SERVICE_INVOCATION_FAILURE, " service:" + weRpcRequest.getInterfaceName());
            }
            WeRpcMessageChecker.check(weRpcRequest, weRpcResponse);
            return weRpcResponse;
        } catch (IOException e) {
            logger.error("调用时有错误发生：", e);
            throw new WeRpcException("服务调用失败: ", e);
        }
    }

}
