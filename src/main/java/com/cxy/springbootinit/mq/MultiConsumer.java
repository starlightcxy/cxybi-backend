package com.cxy.springbootinit.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

public class MultiConsumer {

    private static final String TASK_QUEUE_NAME = "multi_queue";

    public static void main(String[] argv) throws Exception {
        //创建连接
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        final Connection connection = factory.newConnection();

        //2个channel。我觉得1个频道就是1个消费者
        for (int i = 0; i < 2; i++) {
            final Channel channel = connection.createChannel();

            channel.queueDeclare(TASK_QUEUE_NAME, true, false, false, null);
            System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

            //每个channel只能同时处理一个
            channel.basicQos(1);

            //如何处理消息
            int finalI = i;
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");


                try {
                    //处理工作
                    System.out.println(" [x] Received '" + "编号:" + finalI + ":" + message + "'");
                    //第一个参数：指定某条消息，第二个参数：是否批量确认
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                    //停10s，模拟机器处理能力有限
                    Thread.sleep(10000);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                    //二参：是否批量拒绝，三参：拒绝后是否入队。三参false，拒绝了不入队就会丢失。
                    channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, false);
                } finally {
                    System.out.println(" [x] Done");
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                }
            };
            //消费监听。第二个参数false，不自动确认
            channel.basicConsume(TASK_QUEUE_NAME, false, deliverCallback, consumerTag -> {
            });
        }

    }
}