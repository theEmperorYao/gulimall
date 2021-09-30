package com.atguigu.gulimall.order;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 *1、引入amqp场景 RabbitAutoConfiguration就会自动生效
 *2、给容器中配置了
 *  rabbitTemplate AmqpAdmin RabbitConnectionFactoryCreator RabbitMessagingTemplate
 *  所有的属性都是spring.rabbitmq
 *  @ConfigurationProperties(prefix = "spring.rabbitmq")
 *  public class RabbitProperties
 *  3、给配置文件中配置spring.rabbitmq信息
 * 4、@EnableRabbit：Enablexxx
 *
 */

@EnableRabbit
@SpringBootApplication
public class GulimallOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallOrderApplication.class, args);
    }

}
