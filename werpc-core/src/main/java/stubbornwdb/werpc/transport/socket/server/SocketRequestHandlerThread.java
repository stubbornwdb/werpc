package stubbornwdb.werpc.transport.socket.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stubbornwdb.werpc.entity.WeRpcRequest;
import stubbornwdb.werpc.entity.WeRpcResponse;
import stubbornwdb.werpc.handler.RequestHandler;
import stubbornwdb.werpc.serializer.CommonSerializer;
import stubbornwdb.werpc.transport.socket.util.ObjectReader;
import stubbornwdb.werpc.transport.socket.util.ObjectWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * 处理RpcRequest的工作线程
 */
public class SocketRequestHandlerThread implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(SocketRequestHandlerThread.class);

    private Socket socket;
    private RequestHandler requestHandler;
    private CommonSerializer serializer;

    public SocketRequestHandlerThread(Socket socket, RequestHandler requestHandler, CommonSerializer serializer) {
        this.socket = socket;
        this.requestHandler = requestHandler;
        this.serializer = serializer;
    }

    @Override
    public void run() {
        try (InputStream inputStream = socket.getInputStream();
             OutputStream outputStream = socket.getOutputStream()) {
            WeRpcRequest weRpcRequest = (WeRpcRequest) ObjectReader.readObject(inputStream);
            Object result = requestHandler.handle(weRpcRequest);
            WeRpcResponse<Object> response = WeRpcResponse.success(result, weRpcRequest.getRequestId());
            ObjectWriter.writeObject(outputStream, response, serializer);
        } catch (IOException e) {
            logger.error("调用或发送时有错误发生：", e);
        }
    }

}
