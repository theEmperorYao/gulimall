package com.atguigu.gulimall.product.web;

import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.web.Catalog2Vo;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author tangyao
 * @version 1.0.0
 * @Description TODO
 * @createTime 2020年10月18日 15:52:00
 */
@Controller
public class IndexController {

    @Autowired
    CategoryService categoryService;

    @Autowired
    RedissonClient redisson;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @GetMapping({"/", "index.html"})
    public String indexPage(Model model) {
        //todo 1、查出所有的一级分类
        long l = System.currentTimeMillis();
        List<CategoryEntity> categoryEntities = categoryService.getLevel1Categorys();
        System.out.println("消耗时间：" + (System.currentTimeMillis() - l));
        model.addAttribute("categorys", categoryEntities);
        return "index";
    }

    @ResponseBody
    @RequestMapping("/index/catalog.json")
    public Map<String, List<Catalog2Vo>> getCatalogJson() {
        //最大的返回对象是一个json对象，说明他是一个map，map就是一个json对象，所以返回类型为一个map
        //不能写Vo，因为他的key都不确定，
        Map<String, List<Catalog2Vo>> catalogJson = categoryService.getCatalogJson();
//        Map<String, List<Catalog2Vo>> catalogJson = categoryService.getDataFromDB();
//        Map<String, List<Catalog2Vo>> catalogJson = categoryService.getCatalogJsonFromDbWithLocalLock();
//        Map<String, List<Catalog2Vo>> catalogJson = categoryService.getCatalogJson2();
        return catalogJson;

    }

    @ResponseBody
    @GetMapping("/hello")
    public String hello() {
        //1、获取同一把锁，只要锁的名字一样，就是同一把锁，
        RLock lock = redisson.getLock("my-lock");
        //2、加锁
        //阻塞式等待，默认加的锁都是30秒
//        lock.lock();
        //1)、锁的自动续期，如果业务超长，运行期间自动给锁续上30s,不用担心业务时间长，锁自动过期被删掉
        //2)、加锁的业务只要运行完成，就不会给当前续期，即使不手动删除解锁，锁默认在30s以后自动删除。

        //10秒自动解锁，自动解锁时间一定要大于业务的执行的时间
        lock.lock(30, TimeUnit.SECONDS);
        // 问题：在锁时间到了以后，不会自动续期
        //1、如果我们传递了锁的超时时间，就发送给redis执行脚本，进行占锁，默认超时就是我们指定的时间
        //2、如果我们未指定锁的超时时间，就使用 30 * 1000 【看门狗lockWatchdogTimeout的默认时间】
        // 只要占锁成功，就会启动一个定时任务【重新给锁设定过期时间，新的过期时间就是看门狗的默认时间】,每隔10s自动续期，续成30s
        // internalLockLeaseTime / 3【看门狗时间】/3,10s

        //最佳实战
        //1) lock.lock(10, TimeUnit.SECONDS);省掉了整个续期操作，手动解锁
        try {
            System.out.println("加锁成功，执行业务" + Thread.currentThread().getId());
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            //3、解锁 假设解锁代码没有运行，redis会不会出现死锁
            System.out.println(Thread.currentThread().getId() + "释放锁");
            lock.unlock();
        }
        return "hello";
    }

    /**
     * 保证一定能读到最新数据，修改期间，写锁是一个排他锁（互斥锁）。读锁是一个共享锁
     * 写锁没释放，读就必须等待
     * <p>
     * 读 + 读 ：相当于无锁，并发读，只会在redis中记录好，所有当前的读锁，他们都会同时加锁成功
     * 写 + 读 ：等待写锁释放
     * 写 + 写：阻塞方式
     * 读 + 写：有读锁，写也需要等待
     * //只要有写的存在，都必须等待
     *
     * @return
     */
    @GetMapping("/read")
    @ResponseBody
    public String readValue() {

        RReadWriteLock readWriteLock = redisson.getReadWriteLock("rw-lock");
        String s = "";
        RLock rLock = readWriteLock.readLock();
        rLock.lock();
        try {
            System.out.println("读锁加锁成功。。。。" + Thread.currentThread().getId());
            s = stringRedisTemplate.opsForValue().get("writeValue");
            Thread.sleep(30000);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            rLock.unlock();
            System.out.println("读锁释放" + Thread.currentThread().getId());
        }
        return s;
    }


    @GetMapping("/write")
    @ResponseBody
    public String writeValue() {

        RReadWriteLock readWriteLock = redisson.getReadWriteLock("rw-lock");
        String s = "";
        RLock rLock = readWriteLock.writeLock();
        try {
            //1、改数据加写锁，读数据加读锁
            rLock.lock();
            System.out.println("写锁加锁成功。。。。" + Thread.currentThread().getId());
            s = UUID.randomUUID().toString();
            Thread.sleep(30000);
            stringRedisTemplate.opsForValue().set("writeValue", s);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            rLock.unlock();
            System.out.println("写锁释放" + Thread.currentThread().getId());
        }
        return s;
    }


    /**
     * 信号量可以做分布式限流
     *
     * @return
     * @throws InterruptedException
     */
    @GetMapping("/park")
    @ResponseBody
    public String park() throws InterruptedException {

        RSemaphore park = redisson.getSemaphore("park");
        boolean b = park.tryAcquire();
        if (b) {
            //执行业务
        } else {
            return "error";
        }
        return "ok" + b;

    }

    @GetMapping("/go")
    @ResponseBody
    public String go() throws InterruptedException {
        RSemaphore park = redisson.getSemaphore("park");
        park.release();
        return "走了";

    }

    @GetMapping("/lockdoor")
    @ResponseBody
    public String lockDoor() throws InterruptedException {

        RCountDownLatch door = redisson.getCountDownLatch("door");
        door.trySetCount(5);
        door.await();
        return "放假了";
    }

    @GetMapping("/gogogo/{id}")
    @ResponseBody
    public String gogogo(@PathVariable int id) {

        RCountDownLatch door = redisson.getCountDownLatch("door");
        door.countDown();
        return id + "号走了";
    }


}
