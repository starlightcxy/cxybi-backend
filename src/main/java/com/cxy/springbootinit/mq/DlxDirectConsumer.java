package com.cxy.springbootinit.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.util.HashMap;
import java.util.Map;

public class DlxDirectConsumer {

  private static final String DEAD_EXCHANGE_NAME = "dlx-direct-exchange";

  private static final String WORK_EXCHANGE_NAME = "direct2-exchange";

  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();
    //这一行不可以省略，省略报错。
    channel.exchangeDeclare(WORK_EXCHANGE_NAME, "direct");

    //死信队列参数配置
    Map<String, Object> args = new HashMap<String, Object>();
    args.put("x-dead-letter-exchange", DEAD_EXCHANGE_NAME);
    args.put("x-dead-letter-routing-key", "waibao");
    //生产者-"xiaodog"-小狗队列-"waibao"-(dlx_waibao_queue)
    //小狗队列作为中间人，通过args方式和死信队列建立联系，通过绑定方式和业务交换机建立联系
    String queueName = "xiaodog_queue";
    channel.queueDeclare(queueName, true, false, false, args);
    channel.queueBind(queueName, WORK_EXCHANGE_NAME, "xiaodog");

    //小猫队列与老板死信队列
    Map<String, Object> args2 = new HashMap<String, Object>();
    args2.put("x-dead-letter-exchange", DEAD_EXCHANGE_NAME);
    args2.put("x-dead-letter-routing-key", "laoban");
    String queueName2 = "xiaocat_queue";
    channel.queueDeclare(queueName2, true, false, false, args2);
    channel.queueBind(queueName2, WORK_EXCHANGE_NAME, "xiaocat");

    System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

    DeliverCallback xiaodogDeliverCallback = (consumerTag, delivery) -> {
        String message = new String(delivery.getBody(), "UTF-8");
        //拒绝消息，并且不要重新将消息放回队列。这样就可以放入相关联的死信队列
        //虽然basicConsume的autoAck设置为false，但是这一行不可以省略，还是要Nack
        channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, false);
        System.out.println(" [xiaodog] Received '" +
            delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
    };
    DeliverCallback xiaocatDeliverCallback = (consumerTag, delivery) -> {
        String message = new String(delivery.getBody(), "UTF-8");
        channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, false);
        System.out.println(" [xiaocat] Received '" +
              delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
    };

    channel.basicConsume(queueName, false, xiaodogDeliverCallback, consumerTag -> { });
    channel.basicConsume(queueName2, false, xiaocatDeliverCallback, consumerTag -> { });
  }
}