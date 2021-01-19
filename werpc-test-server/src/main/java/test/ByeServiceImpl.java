package test;


import stubbornwdb.werpc.annotation.WeRpcService;
import stubbornwdb.werpc.api.ByeService;

@WeRpcService
public class ByeServiceImpl implements ByeService {

    @Override
    public String bye(String name) {
        return "bye, " + name;
    }
}
