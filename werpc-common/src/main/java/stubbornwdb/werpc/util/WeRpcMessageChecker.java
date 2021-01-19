package stubbornwdb.werpc.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stubbornwdb.werpc.entity.WeRpcRequest;
import stubbornwdb.werpc.entity.WeRpcResponse;
import stubbornwdb.werpc.enumeration.ResponseCode;
import stubbornwdb.werpc.enumeration.WeRpcError;
import stubbornwdb.werpc.exception.WeRpcException;

/**
 * 检查响应与请求
 */
public class WeRpcMessageChecker {

    public static final String INTERFACE_NAME = "interfaceName";
    private static final Logger logger = LoggerFactory.getLogger(WeRpcMessageChecker.class);

    private WeRpcMessageChecker() {
    }

    public static void check(WeRpcRequest weRpcRequest, WeRpcResponse weRpcResponse) {
        if (weRpcResponse == null) {
            logger.error("调用服务失败,serviceName:{}", weRpcRequest.getInterfaceName());
            throw new WeRpcException(WeRpcError.SERVICE_INVOCATION_FAILURE, INTERFACE_NAME + ":" + weRpcRequest.getInterfaceName());
        }

        if (!weRpcRequest.getRequestId().equals(weRpcResponse.getRequestId())) {
            throw new WeRpcException(WeRpcError.RESPONSE_NOT_MATCH, INTERFACE_NAME + ":" + weRpcRequest.getInterfaceName());
        }

        if (weRpcResponse.getStatusCode() == null || !weRpcResponse.getStatusCode().equals(ResponseCode.SUCCESS.getCode())) {
            logger.error("调用服务失败,serviceName:{},RpcResponse:{}", weRpcRequest.getInterfaceName(), weRpcResponse);
            throw new WeRpcException(WeRpcError.SERVICE_INVOCATION_FAILURE, INTERFACE_NAME + ":" + weRpcRequest.getInterfaceName());
        }
    }

}
