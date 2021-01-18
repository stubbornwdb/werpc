package stubbornwdb.werpc.provider;


import lombok.extern.slf4j.Slf4j;
import stubbornwdb.werpc.entity.WeRpcServiceProperties;
import stubbornwdb.werpc.enums.WeRpcErrorMessageEnum;
import stubbornwdb.werpc.exception.WeRpcException;
import stubbornwdb.werpc.extension.ExtensionLoader;
import stubbornwdb.werpc.registry.ServiceRegistry;
import stubbornwdb.werpc.remoting.transport.netty.server.NettyRpcServer;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ServiceProviderImpl implements ServiceProvider {

    /**
     * key: rpc service name(interface name + version + group)
     * value: service object
     */
    private final Map<String, Object> serviceMap;
    private final Set<String> registeredService;
    private final ServiceRegistry serviceRegistry;


    public ServiceProviderImpl() {
        serviceMap = new ConcurrentHashMap<>();
        registeredService = ConcurrentHashMap.newKeySet();
        serviceRegistry = ExtensionLoader.getExtensionLoader(ServiceRegistry.class).getExtension("zk");
    }

    @Override
    public void addService(Object service, Class<?> serviceClass, WeRpcServiceProperties weRpcServiceProperties) {
        String rpcServiceName = weRpcServiceProperties.toRpcServiceName();
        if (registeredService.contains(rpcServiceName)) {
            return;
        }
        registeredService.add(rpcServiceName);
        serviceMap.put(rpcServiceName, service);
        log.info("Add service: {} and interfaces:{}", rpcServiceName, service.getClass().getInterfaces());
    }

    @Override
    public Object getService(WeRpcServiceProperties weRpcServiceProperties) {
        Object service = serviceMap.get(weRpcServiceProperties.toRpcServiceName());
        if (null == service) {
            throw new WeRpcException(WeRpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND);
        }
        return service;
    }

    @Override
    public void publishService(Object service) {
        this.publishService(service, WeRpcServiceProperties.builder().group("").version("").build());
    }

    @Override
    public void publishService(Object service, WeRpcServiceProperties weRpcServiceProperties) {
        try {
            String host = InetAddress.getLocalHost().getHostAddress();
            Class<?> serviceRelatedInterface = service.getClass().getInterfaces()[0];
            String serviceName = serviceRelatedInterface.getCanonicalName();
            weRpcServiceProperties.setServiceName(serviceName);
            this.addService(service, serviceRelatedInterface, weRpcServiceProperties);
            serviceRegistry.registerService(weRpcServiceProperties.toRpcServiceName(), new InetSocketAddress(host, NettyRpcServer.PORT));
        } catch (UnknownHostException e) {
            log.error("occur exception when getHostAddress", e);
        }
    }

}
