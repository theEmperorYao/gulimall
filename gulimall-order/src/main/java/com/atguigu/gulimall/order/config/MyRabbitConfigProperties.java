package com.atguigu.gulimall.order.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @Classname MyRabbitConfigProperties
 * @Description TODO
 * @Date 2021/10/7 1:13 下午
 * @Created by tangyao
 */
@ConfigurationProperties(prefix = "rabbitmq.order")
@Component
@Data
public class MyRabbitConfigProperties {


    private String normalQueue;

    private String delayQueue;

    private String eventExchange;

    private String routingKey;

    private String letterRoutingKey;

    private Integer ttl;

}
