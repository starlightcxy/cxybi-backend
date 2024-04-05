package com.cxy.springbootinit.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

/*
    注意给application加-Dfile.encoding=UTF-8，乱码问题解决掉，不然结果不正确。
 */
public class TopicConsumer {

  private static final String EXCHANGE_NAME = "topic-exchange";

  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();

    channel.exchangeDeclare(EXCHANGE_NAME, "topic");

    String queueName = "frontend_queue";
    channel.queueDeclare(queueName, true, false, false, null);
    channel.queueBind(queueName, EXCHANGE_NAME, "#.前端.#");

    String queueName2 = "backend_queue";
    channel.queueDeclare(queueName2, true, false, false, null);
    channel.queueBind(queueName2, EXCHANGE_NAME, "#.后端.#");

    String queueName3 = "product_queue";
    channel.queueDeclare(queueName3, true, false, false, null);
    channel.queueBind(queueName3, EXCHANGE_NAME, "#.产品.#");

    System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

    //前端人员小a的回调函数
    DeliverCallback xiaoaDeliverCallback = (consumerTag, delivery) -> {
        String message = new String(delivery.getBody(), "UTF-8");
        System.out.println(" [xiaoa] Received '" +
            delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
    };
    //后端人员小b的回调函数
    DeliverCallback xiaobDeliverCallback = (consumerTag, delivery) -> {
      String message = new String(delivery.getBody(), "UTF-8");
      System.out.println(" [xiaob] Received '" +
              delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
    };
    //产品人员小c的回调函数
    DeliverCallback xiaocDeliverCallback = (consumerTag, delivery) -> {
      String message = new String(delivery.getBody(), "UTF-8");
      System.out.println(" [xiaoc] Received '" +
              delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
    };
    channel.basicConsume(queueName, true, xiaoaDeliverCallback, consumerTag -> { });
    channel.basicConsume(queueName2, true, xiaobDeliverCallback, consumerTag -> { });
    channel.basicConsume(queueName3, true, xiaocDeliverCallback, consumerTag -> { });
  }
}