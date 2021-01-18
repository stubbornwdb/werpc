package stubbornwdb.werpc.remoting.transport.netty.client;

import stubbornwdb.werpc.remoting.dto.WeRpcResponse;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * unprocessed requests by the server.
 *
 */
public class UnprocessedRequests {
    private static final Map<String, CompletableFuture<WeRpcResponse<Object>>> UNPROCESSED_RESPONSE_FUTURES = new ConcurrentHashMap<>();

    public void put(String requestId, CompletableFuture<WeRpcResponse<Object>> future) {
        UNPROCESSED_RESPONSE_FUTURES.put(requestId, future);
    }

    public void complete(WeRpcResponse<Object> weRpcResponse) {
        CompletableFuture<WeRpcResponse<Object>> future = UNPROCESSED_RESPONSE_FUTURES.remove(weRpcResponse.getRequestId());
        if (null != future) {
            future.complete(weRpcResponse);
        } else {
            throw new IllegalStateException();
        }
    }
}
