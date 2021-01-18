package stubbornwdb.werpc.remoting.dto;

import lombok.*;
import stubbornwdb.werpc.entity.WeRpcServiceProperties;

import java.io.Serializable;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@ToString
public class WeRpcRequest implements Serializable {
    private static final long serialVersionUID = 1905122041950251207L;
    private String requestId;
    private String interfaceName;
    private String methodName;
    private Object[] parameters;
    private Class<?>[] paramTypes;
    private String version;
    private String group;

    public WeRpcServiceProperties toRpcProperties() {
        return WeRpcServiceProperties.builder().serviceName(this.getInterfaceName())
                .version(this.getVersion())
                .group(this.getGroup()).build();
    }
}
