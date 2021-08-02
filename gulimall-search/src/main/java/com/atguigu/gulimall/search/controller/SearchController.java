package com.atguigu.gulimall.search.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author tangyao
 * @version 1.0.0
 * @Description TODO
 * @createTime 2020年10月25日 13:21:00
 */
@Controller
public class SearchController {

    @GetMapping("/list.html")
    public String listPage() {
        System.out.println("跳转到list.html");
        return "list";
    }
}
