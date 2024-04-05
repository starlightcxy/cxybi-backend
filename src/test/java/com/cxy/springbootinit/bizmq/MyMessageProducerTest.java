package com.cxy.springbootinit.bizmq;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MyMessageProducerTest {

    @Resource
    private MyMessageProducer myMessageProducer;

    //如果参数写的不对，不会报错，会测试成功，但是主程序不会有该条信息的日志
    @Test
    void sendMessage() {
        myMessageProducer.sendMessage("code_exchange", "my_routingKey","你好呀");
    }
}