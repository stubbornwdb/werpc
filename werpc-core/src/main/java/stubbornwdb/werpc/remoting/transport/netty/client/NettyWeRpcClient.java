package stubbornwdb.werpc.remoting.transport.netty.client;


import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import stubbornwdb.werpc.enums.CompressTypeEnum;
import stubbornwdb.werpc.enums.SerializationTypeEnum;
import stubbornwdb.werpc.extension.ExtensionLoader;
import stubbornwdb.werpc.factory.SingletonFactory;
import stubbornwdb.werpc.registry.ServiceDiscovery;
import stubbornwdb.werpc.remoting.constants.WeRpcConstants;
import stubbornwdb.werpc.remoting.dto.WeRpcMessage;
import stubbornwdb.werpc.remoting.dto.WeRpcRequest;
import stubbornwdb.werpc.remoting.dto.WeRpcResponse;
import stubbornwdb.werpc.remoting.transport.WeRpcRequestTransport;
import stubbornwdb.werpc.remoting.transport.netty.codec.WeRpcMessageDecoder;
import stubbornwdb.werpc.remoting.transport.netty.codec.WeRpcMessageEncoder;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * initialize and close Bootstrap object
 *
 */
@Slf4j
public final class NettyWeRpcClient implements WeRpcRequestTransport {
    private final ServiceDiscovery serviceDiscovery;
    private final UnprocessedRequests unprocessedRequests;
    private final ChannelProvider channelProvider;
    private final Bootstrap bootstrap;
    private final EventLoopGroup eventLoopGroup;

    public NettyWeRpcClient() {
        // initialize resources such as EventLoopGroup, Bootstrap
        eventLoopGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                //  The timeout period of the connection.
                //  If this time is exceeded or the connection cannot be established, the connection fails.
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline p = ch.pipeline();
                        // If no data is sent to the server within 15 seconds, a heartbeat request is sent
                        p.addLast(new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS));
                        p.addLast(new WeRpcMessageEncoder());
                        p.addLast(new WeRpcMessageDecoder());
                        p.addLast(new NettyRpcClientHandler());
                    }
                });
        this.serviceDiscovery = ExtensionLoader.getExtensionLoader(ServiceDiscovery.class).getExtension("zk");
        this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
        this.channelProvider = SingletonFactory.getInstance(ChannelProvider.class);
    }

    /**
     * connect server and get the channel ,so that you can send rpc message to server
     *
     * @param inetSocketAddress server address
     * @return the channel
     */
    @SneakyThrows
    public Channel doConnect(InetSocketAddress inetSocketAddress) {
        CompletableFuture<Channel> completableFuture = new CompletableFuture<>();
        bootstrap.connect(inetSocketAddress).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                log.info("The client has connected [{}] successful!", inetSocketAddress.toString());
                completableFuture.complete(future.channel());
            } else {
                throw new IllegalStateException();
            }
        });
        return completableFuture.get();
    }

    @Override
    public Object sendRpcRequest(WeRpcRequest weRpcRequest) {
        // build return value
        CompletableFuture<WeRpcResponse<Object>> resultFuture = new CompletableFuture<>();
        // build rpc service name by rpcRequest
        String rpcServiceName = weRpcRequest.toRpcProperties().toRpcServiceName();
        // get server address
        InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcServiceName);
        // get  server address related channel
        Channel channel = getChannel(inetSocketAddress);
        if (channel.isActive()) {
            // put unprocessed request
            unprocessedRequests.put(weRpcRequest.getRequestId(), resultFuture);
            WeRpcMessage weRpcMessage = new WeRpcMessage();
            weRpcMessage.setData(weRpcRequest);
            weRpcMessage.setCodec(SerializationTypeEnum.PROTOSTUFF.getCode());
            weRpcMessage.setCompress(CompressTypeEnum.GZIP.getCode());
            weRpcMessage.setMessageType(WeRpcConstants.REQUEST_TYPE);
            channel.writeAndFlush(weRpcMessage).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    log.info("client send message: [{}]", weRpcMessage);
                } else {
                    future.channel().close();
                    resultFuture.completeExceptionally(future.cause());
                    log.error("Send failed:", future.cause());
                }
            });
        } else {
            throw new IllegalStateException();
        }

        return resultFuture;
    }

    public Channel getChannel(InetSocketAddress inetSocketAddress) {
        Channel channel = channelProvider.get(inetSocketAddress);
        if (channel == null) {
            channel = doConnect(inetSocketAddress);
            channelProvider.set(inetSocketAddress, channel);
        }
        return channel;
    }

    public void close() {
        eventLoopGroup.shutdownGracefully();
    }
}
