package stubbornwdb.werpc.exception;


import stubbornwdb.werpc.enums.WeRpcErrorMessageEnum;


public class WeRpcException extends RuntimeException {
    public WeRpcException(WeRpcErrorMessageEnum weRpcErrorMessageEnum, String detail) {
        super(weRpcErrorMessageEnum.getMessage() + ":" + detail);
    }

    public WeRpcException(String message, Throwable cause) {
        super(message, cause);
    }

    public WeRpcException(WeRpcErrorMessageEnum weRpcErrorMessageEnum) {
        super(weRpcErrorMessageEnum.getMessage());
    }
}
