package com.example.dubbo.consumer;

import com.example.dubbo.api.GreetingService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller，通过 Dubbo 调用远程 GreetingService
 */
@RestController
public class GreetingController {

    @DubboReference(protocol = "tri")
    private GreetingService greetingService;

    @GetMapping("/greeting")
    public String greeting(@RequestParam(defaultValue = "World") String name) {
        return greetingService.sayHello(name);
    }
}
