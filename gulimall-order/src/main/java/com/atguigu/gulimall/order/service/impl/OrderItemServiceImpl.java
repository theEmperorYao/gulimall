package com.atguigu.gulimall.order.service.impl;


import com.alibaba.fastjson.JSON;
import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.entity.OrderReturnReasonEntity;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.order.dao.OrderItemDao;
import com.atguigu.gulimall.order.entity.OrderItemEntity;
import com.atguigu.gulimall.order.service.OrderItemService;
@RabbitListener(queues = {"hello-java-queue"})
@Service("orderItemService")
public class OrderItemServiceImpl extends ServiceImpl<OrderItemDao, OrderItemEntity> implements OrderItemService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderItemEntity> page = this.page(
                new Query<OrderItemEntity>().getPage(params),
                new QueryWrapper<OrderItemEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * @return void
     * @description queues 声明监听的所有队列
     * org.springframework.amqp.core.Message
     * 参数可以写一下类型
     * 1、Message message:原生消息详细信息。头+体
     * 2、T<发送的消息的类型>
     * 3、Channel channel: 当前传输数据的通道
     * <p>
     * Queue ：可以很多人都来监听。只要收到消息，队列删除消息，而且只要有一个收到此消息
     * 场景
     * 1）、订单服务启动多个;同一个消息，只能有一个客户端收到
     * 2）、只有一个消息完全处理完，方法运行结束，我们就可以接收到下一个消息
     * @version V1.0.0
     * @date 11:16 上午 2021/9/30
     * @author tangyao
     */
//    @RabbitListener(queues = {"hello-java-queue"})
    @RabbitHandler
    public void receiveMessage(Message message,
                               OrderReturnReasonEntity content,
                               Channel channel) {
        byte[] body = message.getBody();
        System.out.println("接收到消息..." + content);
        // 消息头属性信息
        MessageProperties messageProperties = message.getMessageProperties();

//        System.out.println("接收到消息...内容：" + message + "==》类型：" + message.getClass());
//        Thread.sleep(3000);
        System.out.println("消息处理完成==》" + content.getName());
        // channel内按顺序自增的。
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        System.out.println("deliveryTag = " + deliveryTag);
        // 签收货物，非批量模式
        try {
            if (deliveryTag % 2 == 0) {
                //收货
                channel.basicAck(deliveryTag, false);
                System.out.println("签收了货物。。。" + deliveryTag);
            } else {
                //退货 requeue=false 丢弃  requeue=true 发回服务器，服务器重新入队
                //basicNack(long deliveryTag, boolean multiple, boolean requeue)
                channel.basicNack(deliveryTag, false, true);
                //basicReject(long deliveryTag, boolean requeue)
//                channel.basicReject();


                System.out.println("没有签收货物。。。" + deliveryTag);
            }

        } catch (IOException e) {
            // 网络中断
            e.printStackTrace();
        }

    }

    @RabbitHandler
    public void receiveMessage2(OrderEntity content,
                                Channel channel) throws InterruptedException {
        System.out.println("接收到消息..." + content);
    }

}