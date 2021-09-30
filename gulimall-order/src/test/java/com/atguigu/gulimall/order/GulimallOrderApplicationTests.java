package com.atguigu.gulimall.order;

import com.atguigu.gulimall.order.entity.OrderReturnReasonEntity;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

@Slf4j
@SpringBootTest
class GulimallOrderApplicationTests {

    @Autowired
    AmqpAdmin amqpAdmin;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Test
    public void sendMessageTest() {
        // 1、发送消息 如果发送的消息是个对象，我们会使用序列化机制，将对象写出去，对象必须实现Serializable
        OrderReturnReasonEntity reasonEntity = new OrderReturnReasonEntity();
        reasonEntity.setId(1L);
        reasonEntity.setName("哈哈");
        reasonEntity.setCreateTime(new Date());

        String msg = "Hello World !";
        //2、发送的对象类型，可以使一个json
        rabbitTemplate.convertAndSend("hello-java-exchange", "hello.java", reasonEntity);
        log.info("消息发送完成{}", reasonEntity);
    }


    /**
     * @return void
     * @description 1、如何创建Exchange[hello.java.exchange]、Queue、Binding
     * 1）使用AmqpAdmin进行创建
     * 2、如何收发消息
     * @version V1.0.0
     * @date 9:38 上午 2021/9/30
     * @author tangyao
     */
    @Test
    void createExchange() {

        // DirectExchange(String name, boolean durable, boolean autoDelete, Map<String, Object> arguments)
        Exchange directExchange = new DirectExchange("hello-java-exchange", true, false);
        amqpAdmin.declareExchange(directExchange);
        log.info("Exchange[{}]创建成功", "hello-java-exchange");
    }

    @Test
    public void createQueue() {
        Queue queue = new Queue("hello-java-queue", true, false, true);
        amqpAdmin.declareQueue(queue);
        log.info("Queue[{}]创建成功", "hello-java-queue");
    }

    @Test
    public void createBinding() {
        // 将exchange指定的交换机和destination目的地进行绑定，使用routingKey作为指定的路由键
        Binding binding = new Binding(
                "hello-java-queue",
                Binding.DestinationType.QUEUE,
                "hello-java-exchange",
                "hello.java", null);
        amqpAdmin.declareBinding(binding);
        log.info("Binding[{}]创建成功", "hello-java-binding");
    }

}
