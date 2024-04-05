package com.cxy.springbootinit.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.util.Scanner;

//死信队列:就是普通队列，只不过命名为死信，人为的给他定义一个用途并实现，不是新特性
public class DlxDirectProducer {

  private static final String DEAD_EXCHANGE_NAME = "dlx-direct-exchange";

  private static final String WORK_EXCHANGE_NAME = "direct2-exchange";

  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    try (Connection connection = factory.newConnection();
         Channel channel = connection.createChannel()) {
        channel.exchangeDeclare(DEAD_EXCHANGE_NAME, "direct");

        //创建老板的死信队列
        String queueName = "laoban_dlx_queue";
        channel.queueDeclare(queueName, true, false, false, null);
        channel.queueBind(queueName, DEAD_EXCHANGE_NAME, "laoban");

        //创建外包的死信队列
        String queueName2 = "waibao_dlx_queue";
        channel.queueDeclare(queueName2, true, false, false, null);
        channel.queueBind(queueName2, DEAD_EXCHANGE_NAME, "waibao");

        //两个死信队列消费任务，比如：老板队列收到任务，打印消息
        //这段代码要放在scanner循环的外面。不然scanner一直在循环，不会执行下面这段逻辑
        DeliverCallback laobanDeliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [laoban] Received '" +
                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };
        DeliverCallback waibaoDeliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [waibao] Received '" +
                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };

        channel.basicConsume(queueName, true, laobanDeliverCallback, consumerTag -> { });
        channel.basicConsume(queueName2, true, waibaoDeliverCallback, consumerTag -> { });

        Scanner scanner = new Scanner(System.in);
        while(scanner.hasNext()){
            String userInput = scanner.nextLine();
            String[] strings = userInput.split(" ");

            if(strings.length < 1){
                continue;
            }

            String message = strings[0];
            String routingKey = strings[1];

            //2参：本消息给哪条路由。3参：不使用额外的消息属性。4参：将消息内容转换为UTF-8编码的字节数组
            channel.basicPublish(WORK_EXCHANGE_NAME, routingKey, null, message.getBytes("UTF-8"));
            System.out.println(" [x] Sent '" + message + " with routing:" + routingKey + "'");
        }

    }
  }
  //..
}