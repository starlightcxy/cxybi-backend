package com.cxy.springbootinit.mq;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.nio.charset.StandardCharsets;

//一对一消息队列模型中的消息发送者
//频道：客户端，操作队列的，可以使连接复用
public class TtlProducer {
    //静态常量字符串
    private final static String QUEUE_NAME = "ttl_queue";

    public static void main(String[] argv) throws Exception {

        ConnectionFactory factory = new ConnectionFactory();
        //没改用户名、密码、端口，就只设置主机名即可
        factory.setHost("localhost");
        //建与RabbitMQ服务器的连接，连接再建频道
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            //注释掉：频道声明队列
            // channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            String message = "Hello World!";

            // // 给消息指定过期时间,这一段代码会报错的
            // AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
            //         // 设置消息的过期时间为1000毫秒
            //         .expiration("1000")
            //         .build();
            // // 发布消息到指定的交换机（"my-exchange"）和路由键（"routing-key"）
            // // 使用指定的属性（过期时间）和消息内容（UTF-8编码的字节数组）
            // channel.basicPublish("my-exchange", "routing-key", properties, message.getBytes(StandardCharsets.UTF_8));

            //频道发送消息
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes(StandardCharsets.UTF_8));
            System.out.println(" [x] Sent '" + message + "'");
        }
    }
}
