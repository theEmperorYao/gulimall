package com.atguigu.gulimall.ware.config;

import lombok.Data;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @Classname MyRabbitConfig
 * @Description TODO
 * @Date 2021/10/7 10:53 上午
 * @Created by tangyao
 */
@Configuration
@Data
public class MyRabbitConfig {


    @Value("${rabbitmq.ware.normal-queue}")
    private String normalQueue;

    @Value("${rabbitmq.ware.event-exchange}")
    private String eventExchange;

    @Value("${rabbitmq.ware.routing-key}")
    private String routingKey;

    @Value("${rabbitmq.ware.delay-queue}")
    private String delayQueue;

    @Value("${rabbitmq.ware.letter-routing-key}")
    private String letterRoutingKey;

    @Value("${rabbitmq.ware.ttl}")
    private Integer ttl;


    /**
     * 使用JSON序列化机制，进行消息转换
     *
     * @return
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @RabbitListener(queues = "stock.release.stock.queue")
    public void handle(Message message) {

    }


    @Bean
    public Exchange stockEventChange() {
        return new TopicExchange(eventExchange, true, false);
    }

    @Bean
    public Queue stockDelayQueue() {

        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", eventExchange);
        arguments.put("x-dead-letter-routing-key", letterRoutingKey);
        arguments.put("x-message-ttl", ttl);
        return new Queue(delayQueue, true, false, false, arguments);
    }

    @Bean
    public Queue stockReleaseQueue() {
        return new Queue(normalQueue, true, false, false);
    }

    @Bean
    public Binding stockReleaseBinding() {
        return new Binding(normalQueue,
                Binding.DestinationType.QUEUE,
                eventExchange,
                letterRoutingKey, null);
    }

    @Bean
    public Binding stockLockedBinding() {
        return new Binding(delayQueue,
                Binding.DestinationType.QUEUE,
                eventExchange,
                routingKey, null);
    }

}
