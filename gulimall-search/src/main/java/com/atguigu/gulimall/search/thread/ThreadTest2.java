package com.atguigu.gulimall.search.thread;


import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Classname ThreadTest2
 * @Description TODO
 * @Date 2021/8/16 4:08 下午
 * @Created by tangyao
 */
public class ThreadTest2 {

    public static ExecutorService executor = Executors.newFixedThreadPool(10);

    public static void main(String[] args) {


    }


    @Test
    public void method01() throws ExecutionException, InterruptedException {
        System.out.println("main...start");
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            System.out.println("当前线程" + Thread.currentThread().getId());
            int i = 10 / 4;
            System.out.println("运行结束" + i);
            return i;
        }, executor).thenApplyAsync(res -> {
            return res * 2;
        });


        System.out.println("main...end.." + future.get());

    }


    @Test
    public void methodSupplyAsync3() throws ExecutionException, InterruptedException {
        System.out.println("main...start");
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            System.out.println("当前线程" + Thread.currentThread().getId());
            int i = 10 / 4;
            System.out.println("运行结束" + i);
            return i;
        }, executor).thenApplyAsync(res -> {
            return res * 2;
        });


        System.out.println("main...end.." + future.get());

    }

    @Test
    public void methodSupplyAsync2() throws ExecutionException, InterruptedException {
        System.out.println("main...start");
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            System.out.println("当前线程" + Thread.currentThread().getId());
            int i = 10 / 4;
            System.out.println("运行结束" + i);
            return i;
        }, executor).handle((res, exception) -> {
            return res != null ? res * 2 : (exception != null) ? 0 : 0;
        });


        System.out.println("main...end.." + future.get());

    }


    @Test
    public void methodSupplyAsync() throws ExecutionException, InterruptedException {
        System.out.println("main...start");
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            System.out.println("当前线程" + Thread.currentThread().getId());
            int i = 10 / 0;
            System.out.println("运行结束" + i);
            return i;
        }, executor).whenComplete((result, exception) -> {
            System.out.println("结果是 = " + result + " 异常是：" + exception);
        }).exceptionally(throwable -> {
            return 10;
        });


        System.out.println("main...end.." + future.get());

    }


    @Test
    public void methodRunAsync() {
        System.out.println("main...start");
        CompletableFuture.runAsync(() -> {
            System.out.println("当前线程" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运行结束" + i);
        }, executor);
        System.out.println("main...end");
    }


}
