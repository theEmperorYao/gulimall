package com.atguigu.gulimall.search.controller;

import com.atguigu.gulimall.search.service.MallSearchService;
import com.atguigu.gulimall.search.vo.SearchParam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.atguigu.gulimall.search.vo.SearchResult;

import javax.servlet.http.HttpServletRequest;


/**
 * @author tangyao
 * @version 1.0.0
 * @Description TODO
 * @createTime 2020年10月25日 13:21:00
 */
@Controller
public class SearchController {

    @Autowired
    MallSearchService mallSearchService;

    @GetMapping("/list.html")
    public String listPage(SearchParam searchParam, Model model, HttpServletRequest request) {

        // 获取路径原生的查询属性
        searchParam.set_queryString(request.getQueryString());
        SearchResult result = mallSearchService.search(searchParam);
        model.addAttribute("result", result);
        return "list";
    }

}
