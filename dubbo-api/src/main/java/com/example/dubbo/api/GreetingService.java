package com.example.dubbo.api;

/**
 * Dubbo 服务接口定义
 */
public interface GreetingService {

    /**
     * 向指定用户打招呼
     *
     * @param name 用户名称
     * @return 问候语
     */
    String sayHello(String name);
}
