package stubbornwdb.werpc.remoting.dto;

import lombok.*;
import stubbornwdb.werpc.enums.WeRpcResponseCodeEnum;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class WeRpcResponse<T> implements Serializable {

    private static final long serialVersionUID = 715745410605631233L;
    private String requestId;
    /**
     * response code
     */
    private Integer code;
    /**
     * response message
     */
    private String message;
    /**
     * response body
     */
    private T data;

    public static <T> WeRpcResponse<T> success(T data, String requestId) {
        WeRpcResponse<T> response = new WeRpcResponse<>();
        response.setCode(WeRpcResponseCodeEnum.SUCCESS.getCode());
        response.setMessage(WeRpcResponseCodeEnum.SUCCESS.getMessage());
        response.setRequestId(requestId);
        if (null != data) {
            response.setData(data);
        }
        return response;
    }

    public static <T> WeRpcResponse<T> fail(WeRpcResponseCodeEnum weRpcResponseCodeEnum) {
        WeRpcResponse<T> response = new WeRpcResponse<>();
        response.setCode(weRpcResponseCodeEnum.getCode());
        response.setMessage(weRpcResponseCodeEnum.getMessage());
        return response;
    }

}
