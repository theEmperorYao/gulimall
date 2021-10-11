package com.atguigu.gulimall.order.config;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @Classname MyMQConfig
 * @Description TODO
 * @Date 2021/10/6 6:14 下午
 * @Created by tangyao
 */
@Configuration
public class MyMQConfig {


    /**
     * @Bean Binding Queue Exchange 都会自动创建（RabbitMQ没有的情况）
     * <p>
     * RabbitMQ 只要有。@Bean属性发生变化也不会覆盖
     */
    @Bean
    // 死信队列
    public Queue orderDelayQueue(MyRabbitConfigProperties rabbitConfigProperties) {
        // (String name, boolean durable, boolean exclusive, boolean autoDelete,
        //			@Nullable Map<String, Object> arguments)

        /**
         * x-dead-letter-exchange: user.order.exchange
         * x-dead-letter-routing-key: order
         * x-message-ttl: 60000
         */
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", rabbitConfigProperties.getEventExchange());
        arguments.put("x-dead-letter-routing-key", rabbitConfigProperties.getLetterRoutingKey());
        arguments.put("x-message-ttl", rabbitConfigProperties.getTtl());
        Queue queue = new Queue(rabbitConfigProperties.getDelayQueue(), true, false, false, arguments);

        return queue;
    }

    @Bean
    public Queue orderReleaseOrderQueue(MyRabbitConfigProperties rabbitConfigProperties) {
        Queue queue = new Queue(rabbitConfigProperties.getNormalQueue(), true, false, false);

        return queue;
    }

    @Bean
    public Exchange orderEventExchange(MyRabbitConfigProperties rabbitConfigProperties) {
        // String name, boolean durable, boolean autoDelete, Map<String, Object> arguments

        TopicExchange topicExchange = new TopicExchange(rabbitConfigProperties.getEventExchange(), true, false);
        return topicExchange;
    }

    @Bean
    public Binding orderCreateOrderBinding(MyRabbitConfigProperties rabbitConfigProperties) {

//        (String destination, Binding.DestinationType destinationType, String exchange, String routingKey,
//                @Nullable Map<String, Object> arguments)

        HashMap<String, Object> arguments = new HashMap<>();
        Binding binding = new Binding(
                rabbitConfigProperties.getDelayQueue(),
                Binding.DestinationType.QUEUE,
                rabbitConfigProperties.getEventExchange(),
                rabbitConfigProperties.getRoutingKey(),
                null);

        return binding;
    }

    @Bean
    public Binding orderReleaseOrderBinding(MyRabbitConfigProperties rabbitConfigProperties) {

        Binding binding = new Binding(
                rabbitConfigProperties.getNormalQueue(),
                Binding.DestinationType.QUEUE,
                rabbitConfigProperties.getEventExchange(),
                rabbitConfigProperties.getLetterRoutingKey(),
                null);
        return binding;
    }

    /**
     * 订单释放直接和库存释放进行绑定
     *
     * @param rabbitConfigProperties
     * @return
     */
    @Bean
    public Binding stockReleaseOtherBinding(MyRabbitConfigProperties rabbitConfigProperties) {

        Binding binding = new Binding(
                "stock.release.stock.queue",
                Binding.DestinationType.QUEUE,
                rabbitConfigProperties.getEventExchange(),
                "order.release.other.#",
                null);
        return binding;
    }


    @Bean
    public Queue orderSecKillQueue(MyRabbitConfigProperties rabbitConfigProperties) {
        return new Queue(rabbitConfigProperties.getSecKillQueue(), true, false, false);
    }

    @Bean
    public Binding orderSeckillOrderQueueBinding(MyRabbitConfigProperties rabbitConfigProperties) {

        return new Binding(
                rabbitConfigProperties.getSecKillQueue(),
                Binding.DestinationType.QUEUE,
                rabbitConfigProperties.getEventExchange(),
                rabbitConfigProperties.getSecKillRoutingKey(),
                null);
    }


}
