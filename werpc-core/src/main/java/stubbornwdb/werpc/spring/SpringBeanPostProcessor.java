package stubbornwdb.werpc.spring;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;
import stubbornwdb.werpc.annotation.WeRpcReference;
import stubbornwdb.werpc.annotation.WeRpcService;
import stubbornwdb.werpc.entity.WeRpcServiceProperties;
import stubbornwdb.werpc.extension.ExtensionLoader;
import stubbornwdb.werpc.factory.SingletonFactory;
import stubbornwdb.werpc.provider.ServiceProvider;
import stubbornwdb.werpc.provider.ServiceProviderImpl;
import stubbornwdb.werpc.proxy.RpcClientProxy;
import stubbornwdb.werpc.remoting.transport.WeRpcRequestTransport;

import java.lang.reflect.Field;

/**
 * call this method before creating the bean to see if the class is annotated
 *
 */
@Slf4j
@Component
public class SpringBeanPostProcessor implements BeanPostProcessor {

    private final ServiceProvider serviceProvider;
    private final WeRpcRequestTransport rpcClient;

    public SpringBeanPostProcessor() {
        this.serviceProvider = SingletonFactory.getInstance(ServiceProviderImpl.class);
        this.rpcClient = ExtensionLoader.getExtensionLoader(WeRpcRequestTransport.class).getExtension("netty");
    }

    @SneakyThrows
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean.getClass().isAnnotationPresent(WeRpcService.class)) {
            log.info("[{}] is annotated with  [{}]", bean.getClass().getName(), WeRpcService.class.getCanonicalName());
            // get RpcService annotation
            WeRpcService weRpcService = bean.getClass().getAnnotation(WeRpcService.class);
            // build RpcServiceProperties
            WeRpcServiceProperties weRpcServiceProperties = WeRpcServiceProperties.builder()
                    .group(weRpcService.group()).version(weRpcService.version()).build();
            serviceProvider.publishService(bean, weRpcServiceProperties);
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> targetClass = bean.getClass();
        Field[] declaredFields = targetClass.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            WeRpcReference weRpcReference = declaredField.getAnnotation(WeRpcReference.class);
            if (weRpcReference != null) {
                WeRpcServiceProperties weRpcServiceProperties = WeRpcServiceProperties.builder()
                        .group(weRpcReference.group()).version(weRpcReference.version()).build();
                RpcClientProxy rpcClientProxy = new RpcClientProxy(rpcClient, weRpcServiceProperties);
                Object clientProxy = rpcClientProxy.getProxy(declaredField.getType());
                declaredField.setAccessible(true);
                try {
                    declaredField.set(bean, clientProxy);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }

        }
        return bean;
    }
}
