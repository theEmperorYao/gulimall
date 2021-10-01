package com.atguigu.gulimall.order.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @Classname OrderWebController
 * @Description TODO
 * @Date 2021/10/1 4:18 下午
 * @Created by tangyao
 */
@Controller
public class OrderWebController {

    @GetMapping("/toTrade")
    public String toTrade() {
        return "confirm";
    }
}
