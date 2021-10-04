package com.atguigu.gulimall.order;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
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
 *
 *   本地事务失效问题
 *   同一个方法内事务方法互相调用默认失效（原因是绕过了代理对象）事务使用代理对象来控制的
 *   解决：使用代理对象来调用事务方法
 *      1、引入aop-starter ;spring-boot-starter-aop:引入了aspectj
 *      2、@EnableAspectJAutoProxy(exposeProxy = true)；开启 aspectj 动态代理功能 以后的所有动态代理都是aspectJ创建的（及时没有接口也是可以创建动态代理）
 *          对外暴露代理对象
 *      3、本类互调用代理对象
 *         OrderServiceImpl orderService = (OrderServiceImpl) AopContext.currentProxy();
 *         orderService.b();
 *         orderService.c();
 */
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableRedisHttpSession
@EnableFeignClients
@EnableRabbit
@SpringBootApplication
public class GulimallOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallOrderApplication.class, args);
    }

}
