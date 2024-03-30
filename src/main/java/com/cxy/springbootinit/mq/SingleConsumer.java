package com.cxy.springbootinit.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import java.nio.charset.StandardCharsets;

public class SingleConsumer {

    private final static String QUEUE_NAME = "hello";

    public static void main(String[] argv) throws Exception {
        //建工厂、建连接、建频道
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        //声明队列，注意同名的队列参数要一致，即与生产者声明的一致
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        //等待接收
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        //处理消息（得到、打印）
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            //将消息体转为字符串
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(" [x] Received '" + message + "'");
        };
        //消费者，开启消费监听（收消息，传递给deliverCallback，会持续阻塞）
        channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> { });
    }
}