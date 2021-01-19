package stubbornwdb.werpc.transport;


import stubbornwdb.werpc.serializer.CommonSerializer;

/**
 * 服务器类通用接口
 */
public interface WeRpcServer {

    int DEFAULT_SERIALIZER = CommonSerializer.KRYO_SERIALIZER;

    void start();

    <T> void publishService(T service, String serviceName);

}
