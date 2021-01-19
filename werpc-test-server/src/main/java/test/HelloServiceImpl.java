package test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stubbornwdb.werpc.annotation.WeRpcService;
import stubbornwdb.werpc.api.HelloObject;
import stubbornwdb.werpc.api.HelloService;

@WeRpcService
public class HelloServiceImpl implements HelloService {

    private static final Logger logger = LoggerFactory.getLogger(HelloServiceImpl.class);
    @Override
    public String hello(HelloObject object) {
        logger.info("接收到消息：{}", object.getMessage());
        return "这是Impl1方法";
    }

}
