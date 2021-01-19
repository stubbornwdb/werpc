package stubbornwdb.werpc.provider;

/**
 * 保存和提供服务实例对象
 */
public interface ServiceProvider {


    <T> void addServiceProvider(T service, String serviceName);

    Object getServiceProvider(String serviceName);

}
