package stubbornwdb.werpc.loadbalance;


import stubbornwdb.werpc.extension.SPI;

import java.util.List;

/**
 * Interface to the load balancing policy
 */
@SPI
public interface LoadBalance {
    /**
     * Choose one from the list of existing service addresses list
     *
     * @param serviceAddresses Service address list
     * @return target service address
     */
    String selectServiceAddress(List<String> serviceAddresses, String rpcServiceName);
}
