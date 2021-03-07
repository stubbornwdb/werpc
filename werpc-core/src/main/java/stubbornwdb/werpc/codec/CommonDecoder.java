package stubbornwdb.werpc.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stubbornwdb.werpc.entity.WeRpcRequest;
import stubbornwdb.werpc.entity.WeRpcResponse;
import stubbornwdb.werpc.enumeration.PackageType;
import stubbornwdb.werpc.enumeration.WeRpcError;
import stubbornwdb.werpc.exception.WeRpcException;
import stubbornwdb.werpc.serializer.CommonSerializer;

import java.util.List;

/**
 * 通用的解码拦截器
 */
public class CommonDecoder extends ReplayingDecoder {

    private static final Logger logger = LoggerFactory.getLogger(CommonDecoder.class);
    private static final int MAGIC_NUMBER = 0xCAFEBABE;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        //读四个字节的魔数
        int magic = in.readInt();
        //魔数校验
        if (magic != MAGIC_NUMBER) {
            logger.error("不识别的协议包: {}", magic);
            throw new WeRpcException(WeRpcError.UNKNOWN_PROTOCOL);
        }
        //读四个字节的消息包类型
        int packageCode = in.readInt();
        Class<?> packageClass;
        //消息类型校验
        if (packageCode == PackageType.REQUEST_PACK.getCode()) {
            packageClass = WeRpcRequest.class;
        } else if (packageCode == PackageType.RESPONSE_PACK.getCode()) {
            packageClass = WeRpcResponse.class;
        } else {
            logger.error("不识别的数据包: {}", packageCode);
            throw new WeRpcException(WeRpcError.UNKNOWN_PACKAGE_TYPE);
        }
        //读四个字节的序列化类型
        int serializerCode = in.readInt();
        CommonSerializer serializer = CommonSerializer.getByCode(serializerCode);
        if (serializer == null) {
            logger.error("不识别的反序列化器: {}", serializerCode);
            throw new WeRpcException(WeRpcError.UNKNOWN_SERIALIZER);
        }
        //读四个字节的消息长度
        int length = in.readInt();
        byte[] bytes = new byte[length];
        //读定长的消息体
        in.readBytes(bytes);
        //进行反序列化
        Object obj = serializer.deserialize(bytes, packageClass);
        out.add(obj);
    }

}
