package stubbornwdb.werpc.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stubbornwdb.werpc.entity.WeRpcRequest;
import stubbornwdb.werpc.entity.WeRpcResponse;
import stubbornwdb.werpc.enumeration.ResponseCode;
import stubbornwdb.werpc.provider.ServiceProvider;
import stubbornwdb.werpc.provider.ServiceProviderImpl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 进行过程调用的请求处理器
 */
public class RequestHandler {

    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);
    private static final ServiceProvider serviceProvider;

    static {
        serviceProvider = new ServiceProviderImpl();
    }

    /**
     * 调用 invokeTargetMethod 方法处理请求
     * @param weRpcRequest
     * @return
     */
    public Object handle(WeRpcRequest weRpcRequest) {
        //从本地服务表中获取服务类对象
        Object service = serviceProvider.getServiceProvider(weRpcRequest.getInterfaceName());
        return invokeTargetMethod(weRpcRequest, service);
    }

    private Object invokeTargetMethod(WeRpcRequest weRpcRequest, Object service) {
        Object result;
        try {
            //传过来的方法名和参数，构造Method对象
            Method method = service.getClass().getMethod(weRpcRequest.getMethodName(), weRpcRequest.getParamTypes());
            //调用invoke执行方法调用
            result = method.invoke(service, weRpcRequest.getParameters());
            logger.info("服务:{} 成功调用方法:{}", weRpcRequest.getInterfaceName(), weRpcRequest.getMethodName());
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            //调用失败
            return WeRpcResponse.fail(ResponseCode.METHOD_NOT_FOUND, weRpcRequest.getRequestId());
        }
        //返回调用结果
        return result;
    }

}
