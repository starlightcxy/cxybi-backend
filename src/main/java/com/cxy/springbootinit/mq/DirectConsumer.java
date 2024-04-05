package com.cxy.springbootinit.mq;

import com.rabbitmq.client.*;

public class DirectConsumer {

  private static final String EXCHANGE_NAME = "direct-exchange";

  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();
    //这一行不可以省略，省略报错。
    channel.exchangeDeclare(EXCHANGE_NAME, "direct");

    String queueName = "xiaoniu_queue";
    channel.queueDeclare(queueName, true, false, false, null);
    channel.queueBind(queueName, EXCHANGE_NAME, "xiaoniu");

    String queueName2 = "xiaotu_queue";
    channel.queueDeclare(queueName2, true, false, false, null);
    channel.queueBind(queueName2, EXCHANGE_NAME, "xiaotu");



    System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

    DeliverCallback deliverCallback1 = (consumerTag, delivery) -> {
        String message = new String(delivery.getBody(), "UTF-8");
        System.out.println(" [xiaoniu] Received '" +
            delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
    };
    DeliverCallback deliverCallback2 = (consumerTag, delivery) -> {
        String message = new String(delivery.getBody(), "UTF-8");
        System.out.println(" [xiaotu] Received '" +
              delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
    };

    channel.basicConsume(queueName, true, deliverCallback1, consumerTag -> { });
    channel.basicConsume(queueName2, true, deliverCallback2, consumerTag -> { });
  }
}