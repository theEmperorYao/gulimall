package com.atguigu.gulimall.order;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 *1、引入amqp场景 RabbitAutoConfiguration就会自动生效
 *2、给容器中配置了
 *  rabbitTemplate AmqpAdmin RabbitConnectionFactoryCreator RabbitMessagingTemplate
 *  所有的属性都是spring.rabbitmq
 *  @ConfigurationProperties(prefix = "spring.rabbitmq")
 *  public class RabbitProperties
 *  3、给配置文件中配置spring.rabbitmq信息
 * 4、@EnableRabbit：Enablexxx
 * 5、监听消息 使用@RabbitListener
 *  @RabbitListener：类+方法上（监听那些队列即可）
 *  @RabbitHandler：标在方法上(重载区分不同的消息)
 */
@EnableRedisHttpSession
@EnableFeignClients
@EnableRabbit
@SpringBootApplication
public class GulimallOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallOrderApplication.class, args);
    }

}
