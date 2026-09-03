package com.example.dubbo.provider;

import com.example.dubbo.api.GreetingService;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * GreetingService 接口实现，注册为 Dubbo 服务
 */
@DubboService
public class GreetingServiceImpl implements GreetingService {

    @Override
    public String sayHello(String name) {
        return "Hello, " + name + "! This response is from Dubbo Provider.";
    }
}
