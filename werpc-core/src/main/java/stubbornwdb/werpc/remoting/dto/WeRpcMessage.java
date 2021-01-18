package stubbornwdb.werpc.remoting.dto;


import lombok.*;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class WeRpcMessage {

    //rpc message type
    private byte messageType;
    //serialization type
    private byte codec;
    //compress type
    private byte compress;
    //request id
    private int requestId;
    //request data
    private Object data;

}
