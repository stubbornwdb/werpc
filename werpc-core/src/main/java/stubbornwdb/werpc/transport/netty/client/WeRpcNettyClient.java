package stubbornwdb.werpc.transport.netty.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stubbornwdb.werpc.entity.WeRpcRequest;
import stubbornwdb.werpc.entity.WeRpcResponse;
import stubbornwdb.werpc.enumeration.WeRpcError;
import stubbornwdb.werpc.exception.WeRpcException;
import stubbornwdb.werpc.factory.SingletonFactory;
import stubbornwdb.werpc.loadbalancer.LoadBalancer;
import stubbornwdb.werpc.loadbalancer.RandomLoadBalancer;
import stubbornwdb.werpc.registry.NacosServiceDiscovery;
import stubbornwdb.werpc.registry.ServiceDiscovery;
import stubbornwdb.werpc.serializer.CommonSerializer;
import stubbornwdb.werpc.transport.WeRpcClient;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

/**
 * NIO方式消费侧客户端类
 */
public class WeRpcNettyClient implements WeRpcClient {

    private static final Logger logger = LoggerFactory.getLogger(WeRpcNettyClient.class);
    /**
     * EventLoopGroup相当于一个线程池
     */
    private static final EventLoopGroup group;

    /**
     * Bootstarp 启动类
     */
    private static final Bootstrap bootstrap;

    static {
        group = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class);
    }

    /**
     * 服务发现接口
     */
    private final ServiceDiscovery serviceDiscovery;

    /**
     * 序列化器
     */
    private final CommonSerializer serializer;

    /**
     * Map 装载调用请求
     */
    private final UnprocessedRequests unprocessedRequests;


    public WeRpcNettyClient() {
        this(DEFAULT_SERIALIZER, new RandomLoadBalancer());
    }
    public WeRpcNettyClient(LoadBalancer loadBalancer) {
        this(DEFAULT_SERIALIZER, loadBalancer);
    }
    public WeRpcNettyClient(Integer serializer) {
        this(serializer, new RandomLoadBalancer());
    }
    public WeRpcNettyClient(Integer serializer, LoadBalancer loadBalancer) {
        this.serviceDiscovery = new NacosServiceDiscovery(loadBalancer);
        this.serializer = CommonSerializer.getByCode(serializer);
        this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
    }

    /**
     * 发送异步调用请求
     * @param weRpcRequest
     * @return
     */
    @Override
    public CompletableFuture<WeRpcResponse> sendRequest(WeRpcRequest weRpcRequest) {
        if (serializer == null) {
            logger.error("未设置序列化器");
            throw new WeRpcException(WeRpcError.SERIALIZER_NOT_FOUND);
        }
        CompletableFuture<WeRpcResponse> resultFuture = new CompletableFuture<>();
        try {
            //根据调用的接口查找服务
            InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(weRpcRequest.getInterfaceName());

            Channel channel = ChannelProvider.get(inetSocketAddress, serializer);
            if (!channel.isActive()) {
                group.shutdownGracefully();
                return null;
            }
            //请求加入Map
            unprocessedRequests.put(weRpcRequest.getRequestId(), resultFuture);
            //Channel通过网络发送请求
            channel.writeAndFlush(weRpcRequest).addListener((ChannelFutureListener) future1 -> {
                if (future1.isSuccess()) {
                    logger.info(String.format("客户端发送消息: %s", weRpcRequest.toString()));
                } else {
                    future1.channel().close();
                    resultFuture.completeExceptionally(future1.cause());
                    logger.error("发送消息时有错误发生: ", future1.cause());
                }
            });
        } catch (InterruptedException e) {
            unprocessedRequests.remove(weRpcRequest.getRequestId());
            logger.error(e.getMessage(), e);
            Thread.currentThread().interrupt();
        }
        return resultFuture;
    }

}
