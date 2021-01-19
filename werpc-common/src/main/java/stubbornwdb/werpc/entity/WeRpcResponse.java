package stubbornwdb.werpc.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import stubbornwdb.werpc.enumeration.ResponseCode;

import java.io.Serializable;

/**
 * 提供者执行完成或出错后向消费者返回的结果对象
 */
@Data
@NoArgsConstructor
public class WeRpcResponse<T> implements Serializable {

    /**
     * 响应对应的请求号
     */
    private String requestId;
    /**
     * 响应状态码
     */
    private Integer statusCode;
    /**
     * 响应状态补充信息
     */
    private String message;
    /**
     * 响应数据
     */
    private T data;

    public static <T> WeRpcResponse<T> success(T data, String requestId) {
        WeRpcResponse<T> response = new WeRpcResponse<>();
        response.setRequestId(requestId);
        response.setStatusCode(ResponseCode.SUCCESS.getCode());
        response.setData(data);
        return response;
    }

    public static <T> WeRpcResponse<T> fail(ResponseCode code, String requestId) {
        WeRpcResponse<T> response = new WeRpcResponse<>();
        response.setRequestId(requestId);
        response.setStatusCode(code.getCode());
        response.setMessage(code.getMessage());
        return response;
    }

}
