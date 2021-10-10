package com.atguigu.gulimall.coupon;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

//@SpringBootTest
class GulimallCouponApplicationTests {

    @Test
    void contextLoads() {

//        LocalDate now = LocalDate.now();
//        System.out.println("now = " + now);
//        LocalDate plus = now.plusDays(1);
//        System.out.println("plus = " + plus);
//        LocalDate plus2 = now.plusDays(2);
//        System.out.println("plus2 = " + plus2);
//
//        LocalTime min = LocalTime.MIN;
//        System.out.println("min = " + min);
//        LocalTime max = LocalTime.MAX;
//        System.out.println("max = " + max);
//
//        LocalDateTime of = LocalDateTime.of(now, min);
//        System.out.println("of = " + of);
//        LocalDateTime of1 = LocalDateTime.of(plus2, max);
//        System.out.println("of1 = " + of1);


        LocalDate now = LocalDate.now();
        LocalDate plus = now.plusDays(2);
        LocalTime max = LocalTime.MAX;
        LocalDateTime end = LocalDateTime.of(plus, max);
        String format = end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        System.out.println("format = " + format);

    }

}
