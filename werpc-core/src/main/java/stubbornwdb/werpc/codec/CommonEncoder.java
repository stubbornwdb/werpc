package stubbornwdb.werpc.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import stubbornwdb.werpc.entity.WeRpcRequest;
import stubbornwdb.werpc.enumeration.PackageType;
import stubbornwdb.werpc.serializer.CommonSerializer;

/**
 * 通用的编码拦截器
 */
public class CommonEncoder extends MessageToByteEncoder {

    private static final int MAGIC_NUMBER = 0xCAFEBABE;

    private final CommonSerializer serializer;

    public CommonEncoder(CommonSerializer serializer) {
        this.serializer = serializer;
    }

    /**
     * 按照自定义协议的格式进行编码
     * @param ctx
     * @param msg
     * @param out
     * @throws Exception
     */
    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        //魔数
        out.writeInt(MAGIC_NUMBER);
        //消息类型
        if (msg instanceof WeRpcRequest) {
            out.writeInt(PackageType.REQUEST_PACK.getCode());
        } else {
            out.writeInt(PackageType.RESPONSE_PACK.getCode());
        }
        //序列化类型
        out.writeInt(serializer.getCode());
        byte[] bytes = serializer.serialize(msg);
        //序列化后的消息长度
        out.writeInt(bytes.length);
        //序列化后的消息体
        out.writeBytes(bytes);
    }

}
