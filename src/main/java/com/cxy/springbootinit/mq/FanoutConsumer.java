package com.cxy.springbootinit.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

public class FanoutConsumer {
    private static final String EXCHANGE_NAME = "fanout-exchange";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel1 = connection.createChannel();
        Channel channel2 = connection.createChannel();

        channel1.exchangeDeclare(EXCHANGE_NAME, "fanout");
        String queueName = "小王的工作队列";
        channel1.queueDeclare(queueName,true,false,false,null);
        channel1.queueBind(queueName, EXCHANGE_NAME, "");

        String queueName2 = "小刘的工作队列";
        channel2.queueDeclare(queueName2,true,false,false,null);
        channel2.queueBind(queueName2, EXCHANGE_NAME, "");

        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        //创建交付回调函数1
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [小王] Received '" + message + "'");
        };
        //创建交付回调函数2
        DeliverCallback deliverCallback2 = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [小刘] Received '" + message + "'");
        };
        //开始消费消息队列1
        channel1.basicConsume(queueName, true, deliverCallback, consumerTag -> {
        });
        //开始消费消息队列2
        channel2.basicConsume(queueName2, true, deliverCallback2, consumerTag -> {
        });
    }
}