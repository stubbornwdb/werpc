package stubbornwdb.werpc.remoting.transport.netty.server;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import stubbornwdb.werpc.enums.CompressTypeEnum;
import stubbornwdb.werpc.enums.SerializationTypeEnum;
import stubbornwdb.werpc.enums.WeRpcResponseCodeEnum;
import stubbornwdb.werpc.factory.SingletonFactory;
import stubbornwdb.werpc.remoting.constants.WeRpcConstants;
import stubbornwdb.werpc.remoting.dto.WeRpcMessage;
import stubbornwdb.werpc.remoting.dto.WeRpcRequest;
import stubbornwdb.werpc.remoting.dto.WeRpcResponse;
import stubbornwdb.werpc.remoting.handler.WeRpcRequestHandler;

/**
 * Customize the ChannelHandler of the server to process the data sent by the client.
 * <p>
 * 如果继承自 SimpleChannelInboundHandler 的话就不要考虑 ByteBuf 的释放 ，{@link SimpleChannelInboundHandler} 内部的
 * channelRead 方法会替你释放 ByteBuf ，避免可能导致的内存泄露问题。详见《Netty进阶之路 跟着案例学 Netty》
 *
 */
@Slf4j
public class NettyRpcServerHandler extends ChannelInboundHandlerAdapter {

    private final WeRpcRequestHandler rpcRequestHandler;

    public NettyRpcServerHandler() {
        this.rpcRequestHandler = SingletonFactory.getInstance(WeRpcRequestHandler.class);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            if (msg instanceof WeRpcMessage) {
                log.info("server receive msg: [{}] ", msg);
                byte messageType = ((WeRpcMessage) msg).getMessageType();
                WeRpcMessage rpcMessage = new WeRpcMessage();
                rpcMessage.setCodec(SerializationTypeEnum.PROTOSTUFF.getCode());
                rpcMessage.setCompress(CompressTypeEnum.GZIP.getCode());
                if (messageType == WeRpcConstants.HEARTBEAT_REQUEST_TYPE) {
                    rpcMessage.setMessageType(WeRpcConstants.HEARTBEAT_RESPONSE_TYPE);
                    rpcMessage.setData(WeRpcConstants.PONG);
                } else {
                    WeRpcRequest rpcRequest = (WeRpcRequest) ((WeRpcMessage) msg).getData();
                    // Execute the target method (the method the client needs to execute) and return the method result
                    Object result = rpcRequestHandler.handle(rpcRequest);
                    log.info(String.format("server get result: %s", result.toString()));
                    rpcMessage.setMessageType(WeRpcConstants.RESPONSE_TYPE);
                    if (ctx.channel().isActive() && ctx.channel().isWritable()) {
                        WeRpcResponse<Object> rpcResponse = WeRpcResponse.success(result, rpcRequest.getRequestId());
                        rpcMessage.setData(rpcResponse);
                    } else {
                        WeRpcResponse<Object> rpcResponse = WeRpcResponse.fail(WeRpcResponseCodeEnum.FAIL);
                        rpcMessage.setData(rpcResponse);
                        log.error("not writable now, message dropped");
                    }
                }
                ctx.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        } finally {
            //Ensure that ByteBuf is released, otherwise there may be memory leaks
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.READER_IDLE) {
                log.info("idle check happen, so close the connection");
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("server catch exception");
        cause.printStackTrace();
        ctx.close();
    }
}
