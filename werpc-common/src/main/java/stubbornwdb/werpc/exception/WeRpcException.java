package stubbornwdb.werpc.exception;


import stubbornwdb.werpc.enumeration.WeRpcError;

/**
 * RPC调用异常
 */
public class WeRpcException extends RuntimeException {

    public WeRpcException(WeRpcError error, String detail) {
        super(error.getMessage() + ": " + detail);
    }

    public WeRpcException(String message, Throwable cause) {
        super(message, cause);
    }

    public WeRpcException(WeRpcError error) {
        super(error.getMessage());
    }

}
