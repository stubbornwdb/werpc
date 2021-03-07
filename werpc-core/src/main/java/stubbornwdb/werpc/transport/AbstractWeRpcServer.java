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

    /**
     * 扫描服务并发布服务
     */
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
        //从定义的扫描包下进行扫描
        String basePackage = startClass.getAnnotation(WeRpcServiceScan.class).value();
        if("".equals(basePackage)) {
            basePackage = mainClassName.substring(0, mainClassName.lastIndexOf("."));
        }
        Set<Class<?>> classSet = ReflectUtil.getClasses(basePackage);
        for(Class<?> clazz : classSet) {
            //找到使用@WeRpcService注解的类
            if(clazz.isAnnotationPresent(WeRpcService.class)) {
                //获取注解的值 name
                String serviceName = clazz.getAnnotation(WeRpcService.class).name();
                Object obj;
                try {
                    //尝试实例化
                    obj = clazz.newInstance();
                } catch (InstantiationException | IllegalAccessException e) {
                    logger.error("创建 " + clazz + " 时有错误发生");
                    continue;
                }
                //如果name为空
                if("".equals(serviceName)) {
                    //找到该对象实现的所有接口
                    Class<?>[] interfaces = clazz.getInterfaces();
                    for (Class<?> oneInterface: interfaces){
                        //使用接口名发布服务
                        publishService(obj, oneInterface.getCanonicalName());
                    }
                } else {
                    //使用注解定义的name发布服务
                    publishService(obj, serviceName);
                }
            }
        }
    }

    /**
     * 发布服务
     * @param service 服务实例
     * @param serviceName 服务名称
     * @param <T>
     */
    @Override
    public <T> void publishService(T service, String serviceName) {

        //向本地的服务注册表添加服务
        serviceProvider.addServiceProvider(service, serviceName);
        //向注册中心注册服务
        serviceRegistry.register(serviceName, new InetSocketAddress(host, port));
    }

}
