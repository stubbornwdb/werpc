package stubbornwdb.werpc.transport.netty.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import stubbornwdb.werpc.codec.CommonDecoder;
import stubbornwdb.werpc.codec.CommonEncoder;
import stubbornwdb.werpc.hook.ShutdownHook;
import stubbornwdb.werpc.provider.ServiceProviderImpl;
import stubbornwdb.werpc.registry.NacosServiceRegistry;
import stubbornwdb.werpc.serializer.CommonSerializer;
import stubbornwdb.werpc.transport.AbstractWeRpcServer;


import java.util.concurrent.TimeUnit;

/**
 * NIO方式服务提供侧
 */
public class WeRpcNettyServer extends AbstractWeRpcServer {

    /**
     * 序列化器
     */
    private final CommonSerializer serializer;

    public WeRpcNettyServer(String host, int port) {
        this(host, port, DEFAULT_SERIALIZER);
    }

    /**
     * 服务端构造器
     * @param host 地址
     * @param port 端口
     * @param serializer 序列化器
     * serviceRegistry 服务注册中心
     * serviceProvider 本地注册表
     */
    public WeRpcNettyServer(String host, int port, Integer serializer) {
        this.host = host;
        this.port = port;
        serviceRegistry = new NacosServiceRegistry();
        serviceProvider = new ServiceProviderImpl();
        this.serializer = CommonSerializer.getByCode(serializer);
        scanServices();
    }

    @Override
    public void start() {
        ShutdownHook.getShutdownHook().addClearAllHook();
        //接收请求的线程池
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        //处理请求的线程池
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            //启动类
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .option(ChannelOption.SO_BACKLOG, 256)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            //加入心跳处理器、自定义编码器、解码器、服务处理器
                            pipeline.addLast(new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS))
                                    .addLast(new CommonEncoder(serializer))
                                    .addLast(new CommonDecoder())
                                    .addLast(new NettyServerHandler());
                        }
                    });
            //绑定地址、端口
            ChannelFuture future = serverBootstrap.bind(host, port).sync();
            future.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            logger.error("启动服务器时有错误发生: ", e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

}
