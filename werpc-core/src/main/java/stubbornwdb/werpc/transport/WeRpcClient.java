package stubbornwdb.werpc.transport;


import stubbornwdb.werpc.entity.WeRpcRequest;
import stubbornwdb.werpc.serializer.CommonSerializer;

/**
 * 客户端类通用接口
 */
public interface WeRpcClient {

    int DEFAULT_SERIALIZER = CommonSerializer.KRYO_SERIALIZER;

    Object sendRequest(WeRpcRequest weRpcRequest);

}
