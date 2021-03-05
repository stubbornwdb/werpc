package stubbornwdb.werpc.transport.netty.client;


import stubbornwdb.werpc.entity.WeRpcResponse;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 使用ConcurrentHashMap装载客户端的异步调用请求
 */
public class UnprocessedRequests {

    private static ConcurrentHashMap<String, CompletableFuture<WeRpcResponse>> unprocessedResponseFutures = new ConcurrentHashMap<>();

    public void put(String requestId, CompletableFuture<WeRpcResponse> future) {
        unprocessedResponseFutures.put(requestId, future);
    }

    public void remove(String requestId) {
        unprocessedResponseFutures.remove(requestId);
    }

    public void complete(WeRpcResponse weRpcResponse) {
        CompletableFuture<WeRpcResponse> future = unprocessedResponseFutures.remove(weRpcResponse.getRequestId());
        if (null != future) {
            future.complete(weRpcResponse);
        } else {
            throw new IllegalStateException();
        }
    }

}
