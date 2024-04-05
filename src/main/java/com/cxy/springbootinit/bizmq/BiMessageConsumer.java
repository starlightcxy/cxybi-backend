package com.cxy.springbootinit.bizmq;

import com.cxy.springbootinit.common.ErrorCode;
import com.cxy.springbootinit.constant.BiMqConstant;
import com.cxy.springbootinit.exception.BusinessException;
import com.cxy.springbootinit.manager.AiManager;
import com.cxy.springbootinit.model.entity.Chart;
import com.cxy.springbootinit.service.ChartService;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.cxy.springbootinit.constant.CommonConstant.BI_MODEL_ID;

@Component
@Slf4j //生成日志记录器
public class BiMessageConsumer {

    @Resource
    private ChartService chartService;

    @Resource
    private AiManager aiManager;


    /**
     *
     * @param message 接收到的消息内容，是一个字符串类型
     * @param channel 消息所在的通道，可以通过该通道与 RabbitMQ 进行交互，例如手动确认消息、拒绝消息等
     * @param deliveryTag 消息的投递标签，用于唯一标识一条消息的投递状态和顺序
     */
    @SneakyThrows //使用@SneakyThrows注解简化异常处理
    @RabbitListener(queues = {BiMqConstant.BI_QUEUE_NAME}, ackMode = "MANUAL")
    //通过使用@Header(AmqpHeaders.DELIVERY_TAG)方法参数注解,可以从消息头中提取出该投递标签,并将其赋值给long deliveryTag参数。
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag){

        log.info("receiveMessage message = {}", message);

        if(StringUtils.isBlank(message)){
            channel.basicNack(deliveryTag, false, false);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "消息为空");
        }

        long chartId = Long.parseLong(message);
        Chart chart = chartService.getById(chartId);

        if(chart == null){
            channel.basicNack(deliveryTag, false, false);
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "图表为空");
        }

        //设置执行中状态，更改失败设置出执行信息
        Chart updateChart = new Chart();
        updateChart.setId(chart.getId());
        updateChart.setStatus("running");
        boolean b = chartService.updateById(updateChart);
        if(!b){
            channel.basicNack(deliveryTag, false, false);
            handleChartUpdateError(chart.getId(), "更改图表执行中状态失败");
            return;
        }

        //ai服务
        String result = aiManager.doChat(BI_MODEL_ID, buildUserInput(chart));
        String[] splits = result.split("【【【【【");
        if(splits.length < 3){
            channel.basicNack(deliveryTag, false, false);
            handleChartUpdateError(chart.getId(), "AI生成错误");
            return;
        }
        String genChart = splits[1].trim();
        String genResult = splits[2].trim();

        //设置成功状态，以及生成的图表与结论,更改失败则给出信息
        Chart updateChartResult = new Chart();
        updateChartResult.setId(chart.getId());
        updateChartResult.setStatus("succeed");
        updateChartResult.setGenChart(genChart);
        updateChartResult.setGenResult(genResult);
        boolean updateResult = chartService.updateById(updateChartResult);
        if(!updateResult){
            channel.basicNack(deliveryTag, false, false);
            handleChartUpdateError(chart.getId(), "更改图表成功状态失败");
        }
        //ai处理成功，即任务成功。消息确认
        channel.basicAck(deliveryTag, false);
    }

    //构造用户输入
    private String buildUserInput(Chart chart){

        String goal = chart.getGoal();
        String chartType = chart.getChartType();
        String csvData = chart.getChartData();

        //构造用户输入
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求：").append("\n");

        //拼接分析目标
        String userGoal = goal;
        if(StringUtils.isNotBlank(goal)){
            userGoal += "，请使用" + chartType;
        }

        userInput.append(userGoal).append("\n");
        userInput.append("原始数据：").append("\n");
        userInput.append(csvData).append("\n");

        return userInput.toString();
    }

    //因为异步中有很多地方，比如更改失败，都要将状态设为失败，并且给出执行信息。所以抽象出一个方法
    private void handleChartUpdateError(long chartId, String execMessage){
        Chart updateChartResult = new Chart();
        updateChartResult.setId(chartId);
        updateChartResult.setStatus("failed");
        updateChartResult.setExecMessage(execMessage);
        boolean updateResult = chartService.updateById(updateChartResult);
        if(!updateResult){
            log.error("更改如表失败状态的这个操作失败" + chartId + "，" + execMessage);
        }
    }
}
