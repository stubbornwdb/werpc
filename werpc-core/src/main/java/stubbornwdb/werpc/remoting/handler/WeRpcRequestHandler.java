package stubbornwdb.werpc.remoting.handler;

import lombok.extern.slf4j.Slf4j;
import stubbornwdb.werpc.exception.WeRpcException;
import stubbornwdb.werpc.factory.SingletonFactory;
import stubbornwdb.werpc.provider.ServiceProvider;
import stubbornwdb.werpc.provider.ServiceProviderImpl;
import stubbornwdb.werpc.remoting.dto.WeRpcRequest;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * RpcRequest processor
 *
 */
@Slf4j
public class WeRpcRequestHandler {
    private final ServiceProvider serviceProvider;

    public WeRpcRequestHandler() {
        serviceProvider = SingletonFactory.getInstance(ServiceProviderImpl.class);
    }

    /**
     * Processing rpcRequest: call the corresponding method, and then return the method
     */
    public Object handle(WeRpcRequest weRpcRequest) {
        Object service = serviceProvider.getService(weRpcRequest.toRpcProperties());
        return invokeTargetMethod(weRpcRequest, service);
    }

    /**
     * get method execution results
     *
     * @param weRpcRequest client request
     * @param service    service object
     * @return the result of the target method execution
     */
    private Object invokeTargetMethod(WeRpcRequest weRpcRequest, Object service) {
        Object result;
        try {
            Method method = service.getClass().getMethod(weRpcRequest.getMethodName(), weRpcRequest.getParamTypes());
            result = method.invoke(service, weRpcRequest.getParameters());
            log.info("service:[{}] successful invoke method:[{}]", weRpcRequest.getInterfaceName(), weRpcRequest.getMethodName());
        } catch (NoSuchMethodException | IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
            throw new WeRpcException(e.getMessage(), e);
        }
        return result;
    }
}
