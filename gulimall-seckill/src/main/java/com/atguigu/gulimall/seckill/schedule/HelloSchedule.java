package com.atguigu.gulimall.seckill.schedule;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @Classname HelloSchedule
 * @Description TODO
 * <p>
 * <p>
 *     定时任务
 *          1、@EnableScheduling开启定时任务
 *          2、@Scheduled 开启一个定时任务
 *          3、自动配置类TaskSchedulingAutoConfiguration
 *
 *     异步任务
 *          1、@EnableAsync 开启异步任务功能
 *          2、@Async 希望异步执行的方法上标注
 *          3、自动配置类 TaskExecutionAutoConfiguration 属性绑定在 TaskExecutionProperties
 *
 *        解决异步+定时任务来保证定时任务不阻塞的功能
 *
 * @Date 2021/10/10 3:19 下午
 * @Created by tangyao
 */
@Slf4j
@Component
public class HelloSchedule {

    /**
     * 1、Spring 中6位组成，不允许第七位的年
     * 2、在周几的位置，1-7代表周一到周日：mon-sun
     * 3、定时任务不应该阻塞，默认是阻塞的
     *  1)、可以让业务运行，以异步的方式，自己提交到线程池
     *    CompletableFuture.runAsync(()->{
     *            xxxservice.hello();
     *    },executor);
     *  2）、支持定时任务线程池 设置TaskSchedulingProperties
     *      spring.task.scheduling.pool.size=5（不好使）
     *
     *  3）、让定时任务异步执行
     *
     */
    @Async
    @Scheduled(cron = "* * *  ? * 7 ")
    public void hello() throws InterruptedException {
        log.info("hello...");
        Thread.sleep(3000);


    }

}
