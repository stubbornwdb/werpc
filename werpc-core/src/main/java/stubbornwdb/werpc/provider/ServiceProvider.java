package stubbornwdb.werpc.provider;


import stubbornwdb.werpc.entity.WeRpcServiceProperties;

/**
 * store and provide service object.
 */
public interface ServiceProvider {

    /**
     * @param service              service object
     * @param serviceClass         the interface class implemented by the service instance object
     * @param weRpcServiceProperties service related attributes
     */
    void addService(Object service, Class<?> serviceClass, WeRpcServiceProperties weRpcServiceProperties);

    /**
     * @param weRpcServiceProperties service related attributes
     * @return service object
     */
    Object getService(WeRpcServiceProperties weRpcServiceProperties);

    /**
     * @param service              service object
     * @param weRpcServiceProperties service related attributes
     */
    void publishService(Object service, WeRpcServiceProperties weRpcServiceProperties);

    /**
     * @param service service object
     */
    void publishService(Object service);
}
