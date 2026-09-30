package com.example.dubbo.consumer;

import com.example.dubbo.api.GreetingService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller，通过 Dubbo 协议和 Triple 协议调用远程 GreetingService
 */
@RestController
public class GreetingController {

    @DubboReference(protocol = "dubbo")
    private GreetingService greetingServiceDubbo;

    @DubboReference(protocol = "tri")
    private GreetingService greetingServiceTriple;

    // 通过 Dubbo 协议调用
    @GetMapping("/greeting/dubbo")
    public String greetingByDubbo(@RequestParam(defaultValue = "World") String name) {
        return greetingServiceDubbo.sayHello(name);
    }

    // 通过 Triple 协议调用
    @GetMapping("/greeting/triple")
    public String greetingByTriple(@RequestParam(defaultValue = "World") String name) {
        return greetingServiceTriple.sayHello(name);
    }
}
