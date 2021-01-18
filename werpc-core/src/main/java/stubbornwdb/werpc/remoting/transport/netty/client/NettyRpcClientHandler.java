package stubbornwdb.werpc.remoting.transport.netty.client;

import io.netty.channel.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import stubbornwdb.werpc.enums.CompressTypeEnum;
import stubbornwdb.werpc.enums.SerializationTypeEnum;
import stubbornwdb.werpc.factory.SingletonFactory;
import stubbornwdb.werpc.remoting.constants.WeRpcConstants;
import stubbornwdb.werpc.remoting.dto.WeRpcMessage;
import stubbornwdb.werpc.remoting.dto.WeRpcResponse;

import java.net.InetSocketAddress;

/**
 * Customize the client ChannelHandler to process the data sent by the server
 *
 * <p>
 * 如果继承自 SimpleChannelInboundHandler 的话就不要考虑 ByteBuf 的释放 ，{@link SimpleChannelInboundHandler} 内部的
 * channelRead 方法会替你释放 ByteBuf ，避免可能导致的内存泄露问题。详见《Netty进阶之路 跟着案例学 Netty》
 *
 */
@Slf4j
public class NettyRpcClientHandler extends ChannelInboundHandlerAdapter {
    private final UnprocessedRequests unprocessedRequests;
    private final NettyWeRpcClient nettyRpcClient;

    public NettyRpcClientHandler() {
        this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
        this.nettyRpcClient = SingletonFactory.getInstance(NettyWeRpcClient.class);
    }

    /**
     * Read the message transmitted by the server
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            log.info("client receive msg: [{}]", msg);
            if (msg instanceof WeRpcMessage) {
                WeRpcMessage tmp = (WeRpcMessage) msg;
                byte messageType = tmp.getMessageType();
                if (messageType == WeRpcConstants.HEARTBEAT_RESPONSE_TYPE) {
                    log.info("heart [{}]", tmp.getData());
                } else if (messageType == WeRpcConstants.RESPONSE_TYPE) {
                    WeRpcResponse<Object> weRpcResponse = (WeRpcResponse<Object>) tmp.getData();
                    unprocessedRequests.complete(weRpcResponse);
                }
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.WRITER_IDLE) {
                log.info("write idle happen [{}]", ctx.channel().remoteAddress());
                Channel channel = nettyRpcClient.getChannel((InetSocketAddress) ctx.channel().remoteAddress());
                WeRpcMessage weRpcMessage = new WeRpcMessage();
                weRpcMessage.setCodec(SerializationTypeEnum.PROTOSTUFF.getCode());
                weRpcMessage.setCompress(CompressTypeEnum.GZIP.getCode());
                weRpcMessage.setMessageType(WeRpcConstants.HEARTBEAT_REQUEST_TYPE);
                weRpcMessage.setData(WeRpcConstants.PING);
                channel.writeAndFlush(weRpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    /**
     * Called when an exception occurs in processing a client message
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("client catch exception：", cause);
        cause.printStackTrace();
        ctx.close();
    }

}

