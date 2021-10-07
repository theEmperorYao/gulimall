package com.atguigu.gulimall.order;

import io.seata.spring.annotation.GlobalTransactional;
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
 *
 *    https://seata.io/zh-cn/docs/user/quickstart.html
 *    Seata控制分布式事务
 *    1、每一个微服务先创建undo_log;
 *    2、安装事务协调器 https://github.com/seata/seata/releases
 *    3、整合
 *      1、导入依赖spring-cloud-starter-alibaba-seata seata-all 1.0.0
 *      2、启动seata-server:
 *          registry.conf :注册中心配置 修改 registry  type = "nacos"
 *          file.conf:
 *      3、所有想要用到分布式事务的微服务使用seata DataSourceProxy 代理自己的数据源
 *      4、每个微服务都不是必须导入 修改file.conf：vgroup_mapping.{当前应用的名字}-fescar-service-group
 *      5、启动测试分布式事务
 *      6、给分布式大事务入口标注 @GlobalTransactional
 *      7、每一个远程的小事务用 @Transactional
 */
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableRedisHttpSession
@EnableFeignClients
@EnableRabbit
@SpringBootApplication()
public class GulimallOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallOrderApplication.class, args);
    }

}
