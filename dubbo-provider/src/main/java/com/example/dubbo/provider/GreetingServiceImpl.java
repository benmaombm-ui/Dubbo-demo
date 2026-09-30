package com.example.dubbo.provider;

import com.example.dubbo.api.GreetingService;
import org.apache.dubbo.config.annotation.DubboService;
import org.apache.dubbo.rpc.RpcContext;

/**
 * GreetingService 接口实现，注册为 Dubbo 服务
 */
@DubboService(protocol = {"dubbo", "triple"})
public class GreetingServiceImpl implements GreetingService {

    @Override
    public String sayHello(String name) {
        String clientAppName = RpcContext.getServiceContext().getRemoteApplicationName();
        return "Hello, " + name + "! This response is from Dubbo Provider." + " Client app name is " + clientAppName;
    }
}
