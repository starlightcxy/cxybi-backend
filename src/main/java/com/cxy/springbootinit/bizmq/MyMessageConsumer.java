package com.cxy.springbootinit.bizmq;

import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@Slf4j //生成日志记录器
public class MyMessageConsumer{

    /**
     *
     * @param message 接收到的消息内容，是一个字符串类型
     * @param channel 消息所在的通道，可以通过该通道与 RabbitMQ 进行交互，例如手动确认消息、拒绝消息等
     * @param deliveryTag 消息的投递标签，用于唯一标识一条消息的投递状态和顺序
     */
    @SneakyThrows //使用@SneakyThrows注解简化异常处理
    @RabbitListener(queues = {"code_queue"}, ackMode = "MANUAL")
    //通过使用@Header(AmqpHeaders.DELIVERY_TAG)方法参数注解,可以从消息头中提取出该投递标签,并将其赋值给long deliveryTag参数。
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag){
        log.info("receiveMessage message = {}", message);
        channel.basicAck(deliveryTag, false);
    }
}
