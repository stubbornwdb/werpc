package stubbornwdb.werpc.serializer;

import java.io.IOException;

/**
 * 通用的序列化反序列化接口
 */
public interface CommonSerializer {

    Integer KRYO_SERIALIZER = 0;
    Integer JSON_SERIALIZER = 1;
    Integer HESSIAN_SERIALIZER = 2;
    Integer PROTOBUF_SERIALIZER = 3;
    Integer SMARTBUF_SERIALIZER = 4;

    Integer DEFAULT_SERIALIZER = KRYO_SERIALIZER;

    static CommonSerializer getByCode(int code) {
        switch (code) {
            case 0:
                return new KryoSerializer();
            case 1:
                return new JsonSerializer();
            case 2:
                return new HessianSerializer();
            case 3:
                return new ProtobufSerializer();
            case 4:
                return new SmartBufSerializer();
            default:
                return null;
        }
    }

    byte[] serialize(Object obj) throws IOException;

    Object deserialize(byte[] bytes, Class<?> clazz) throws IOException;

    int getCode();

}
