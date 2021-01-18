package stubbornwdb.werpc.proxy;

/**
 * @Author: wu
 * @Date: 2021/1/18 8:47
 * @Description:
 */
public interface ClientStubProxyFactory {
    <T> T getProxy(Class<T> clazz);
}
