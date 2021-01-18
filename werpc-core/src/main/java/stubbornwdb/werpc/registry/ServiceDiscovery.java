package stubbornwdb.werpc.registry;


import stubbornwdb.werpc.extension.SPI;

import java.net.InetSocketAddress;

/**
 * service discovery
 *
 */
@SPI
public interface ServiceDiscovery {
    /**
     * lookup service by rpcServiceName
     *
     * @param rpcServiceName rpc service name
     * @return service address
     */
    InetSocketAddress lookupService(String rpcServiceName);
}
