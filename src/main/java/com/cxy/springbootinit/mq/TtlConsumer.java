package com.cxy.springbootinit.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class TtlConsumer {

    private final static String QUEUE_NAME = "ttl_queue";

    public static void main(String[] argv) throws Exception {
        //建工厂、建连接、建频道
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        //给队列指定过期时间
        Map<String, Object> args = new HashMap<String, Object>();
        //5s过期
        args.put("x-message-ttl", 5000);
        channel.queueDeclare(QUEUE_NAME, false, false, false, args);//队列名别忘记改

        //等待接收
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        //处理消息（得到、打印）
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            //将消息体转为字符串
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(" [x] Received '" + message + "'");
        };
        //消费者，开启消费监听（收消息，传递给deliverCallback，会持续阻塞）(autoAck ture false)
        channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> { });
    }
}