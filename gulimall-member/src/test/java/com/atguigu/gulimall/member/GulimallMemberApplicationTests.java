package com.atguigu.gulimall.member;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.Md5Crypt;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.nio.charset.StandardCharsets;

//@SpringBootTest
class GulimallMemberApplicationTests {

    @Test
    void contextLoads() {
        // 彩虹表
        String s = DigestUtils.md5Hex("tangyao帅哥");
        System.out.println("s = " + s);

        // 密码要进行加密存储.加盐：$1$ + 8位字符
        // $1$qqqqqqqq$AZofg3QwurbxV3KEOzwuI1
        // 验证 123456 进行盐值加密 但是还是要在数据库维护一个盐值 比较麻烦
        String s1 = Md5Crypt.md5Crypt("123456".getBytes(),"$1$qqqqqqqq");
        System.out.println("s1 = " + s1);

        BCryptPasswordEncoder bCryptPasswordEncoder=new BCryptPasswordEncoder();
        String encode = bCryptPasswordEncoder.encode("123456");
        System.out.println("encode = " + encode);

        boolean matches = bCryptPasswordEncoder.matches("123456",
                "$2a$10$SShkoV3J6/kzB.hHmEUSGOQTuo42hzIr3nPOap/YK.UKz8Wm3ORpC");
        System.out.println("matches = " + matches);

    }

}
