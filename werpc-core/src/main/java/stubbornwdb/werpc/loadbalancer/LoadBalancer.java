package stubbornwdb.werpc.loadbalancer;

import com.alibaba.nacos.api.naming.pojo.Instance;

import java.util.List;

public interface LoadBalancer {

    Instance select(List<Instance> instances);

}
