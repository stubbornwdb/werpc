package stubbornwdb.werpc.serializer;

import com.github.smartbuf.SmartStream;
import stubbornwdb.werpc.enumeration.SerializerCode;

import java.io.IOException;

/**
 * @Author: wu
 * @Date: 2021/1/19 16:36
 * @Description:
 */
public class SmartBufSerializer implements CommonSerializer{
    @Override
    public byte[] serialize(Object obj) throws IOException {
        final SmartStream stream = new SmartStream();
        return stream.serialize(obj);
    }

    @Override
    public Object deserialize(byte[] bytes, Class<?> clazz) throws IOException {
        final SmartStream stream = new SmartStream();
        stream.deserialize(bytes, clazz);
        return null;
    }

    @Override
    public int getCode() {
        return SerializerCode.valueOf("SMARTBUF_SERIALIZER").getCode();
    }
}
