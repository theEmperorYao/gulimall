package com.atguigu.gulimall.ware;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootVersion;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.SpringVersion;

import javax.swing.*;

@SpringBootTest
class GulimallWareApplicationTests {

    @Test
    void contextLoads() {
        String version = SpringVersion.getVersion();
        System.out.println("version = " + version);
        String version1 = SpringBootVersion.getVersion();
        System.out.println("version1 = " + version1);
    }


}
