package stubbornwdb.werpc.remoting.transport;


import stubbornwdb.werpc.extension.SPI;
import stubbornwdb.werpc.remoting.dto.WeRpcRequest;

@SPI
public interface WeRpcRequestTransport {
    /**
     * send rpc request to server and get result
     *
     * @param weRpcRequest message body
     * @return data from server
     */
    Object sendRpcRequest(WeRpcRequest weRpcRequest);
}
