package com.cxy.springbootinit.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.nio.charset.StandardCharsets;

//一对一消息队列模型中的消息发送者
//频道：客户端，操作队列的，可以使连接复用
public class SingleProducer {
    //静态常量字符串，队列名hello
    private final static String QUEUE_NAME = "hello";

    public static void main(String[] argv) throws Exception {

        ConnectionFactory factory = new ConnectionFactory();
        //没改用户名、密码、端口，就只设置主机名即可
        factory.setHost("localhost");
        //建与RabbitMQ服务器的连接，连接再建频道
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            //频道声明队列
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            String message = "Hello World!";
            //频道发送消息
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes(StandardCharsets.UTF_8));
            System.out.println(" [x] Sent '" + message + "'");
        }
    }
}
