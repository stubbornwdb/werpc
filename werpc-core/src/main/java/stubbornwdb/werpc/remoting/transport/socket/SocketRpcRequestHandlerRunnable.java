package stubbornwdb.werpc.remoting.transport.socket;



import lombok.extern.slf4j.Slf4j;
import stubbornwdb.werpc.factory.SingletonFactory;
import stubbornwdb.werpc.remoting.dto.WeRpcRequest;
import stubbornwdb.werpc.remoting.dto.WeRpcResponse;
import stubbornwdb.werpc.remoting.handler.WeRpcRequestHandler;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

@Slf4j
public class SocketRpcRequestHandlerRunnable implements Runnable {
    private final Socket socket;
    private final WeRpcRequestHandler weRpcRequestHandler;


    public SocketRpcRequestHandlerRunnable(Socket socket) {
        this.socket = socket;
        this.weRpcRequestHandler = SingletonFactory.getInstance(WeRpcRequestHandler.class);
    }

    @Override
    public void run() {
        log.info("server handle message from client by thread: [{}]", Thread.currentThread().getName());
        try (ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream())) {
            WeRpcRequest weRpcRequest = (WeRpcRequest) objectInputStream.readObject();
            Object result = weRpcRequestHandler.handle(weRpcRequest);
            objectOutputStream.writeObject(WeRpcResponse.success(result, weRpcRequest.getRequestId()));
            objectOutputStream.flush();
        } catch (IOException | ClassNotFoundException e) {
            log.error("occur exception:", e);
        }
    }

}
