package stubbornwdb.werpc.transport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stubbornwdb.werpc.annotation.WeRpcService;
import stubbornwdb.werpc.annotation.WeRpcServiceScan;
import stubbornwdb.werpc.enumeration.WeRpcError;
import stubbornwdb.werpc.exception.WeRpcException;
import stubbornwdb.werpc.provider.ServiceProvider;
import stubbornwdb.werpc.registry.ServiceRegistry;
import stubbornwdb.werpc.util.ReflectUtil;

import java.net.InetSocketAddress;
import java.util.Set;


public abstract class AbstractWeRpcServer implements WeRpcServer {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    protected String host;
    protected int port;

    protected ServiceRegistry serviceRegistry;
    protected ServiceProvider serviceProvider;

    public void scanServices() {
        String mainClassName = ReflectUtil.getStackTrace();
        Class<?> startClass;
        try {
            startClass = Class.forName(mainClassName);
            if(!startClass.isAnnotationPresent(WeRpcServiceScan.class)) {
                logger.error("启动类缺少 @ServiceScan 注解");
                throw new WeRpcException(WeRpcError.SERVICE_SCAN_PACKAGE_NOT_FOUND);
            }
        } catch (ClassNotFoundException e) {
            logger.error("出现未知错误");
            throw new WeRpcException(WeRpcError.UNKNOWN_ERROR);
        }
        String basePackage = startClass.getAnnotation(WeRpcServiceScan.class).value();
        if("".equals(basePackage)) {
            basePackage = mainClassName.substring(0, mainClassName.lastIndexOf("."));
        }
        Set<Class<?>> classSet = ReflectUtil.getClasses(basePackage);
        for(Class<?> clazz : classSet) {
            if(clazz.isAnnotationPresent(WeRpcService.class)) {
                String serviceName = clazz.getAnnotation(WeRpcService.class).name();
                Object obj;
                try {
                    obj = clazz.newInstance();
                } catch (InstantiationException | IllegalAccessException e) {
                    logger.error("创建 " + clazz + " 时有错误发生");
                    continue;
                }
                if("".equals(serviceName)) {
                    Class<?>[] interfaces = clazz.getInterfaces();
                    for (Class<?> oneInterface: interfaces){
                        publishService(obj, oneInterface.getCanonicalName());
                    }
                } else {
                    publishService(obj, serviceName);
                }
            }
        }
    }

    @Override
    public <T> void publishService(T service, String serviceName) {
        serviceProvider.addServiceProvider(service, serviceName);
        serviceRegistry.register(serviceName, new InetSocketAddress(host, port));
    }

}
