package com.atguigu.gulimall.search.service;

import com.atguigu.gulimall.search.vo.SearchParam;
import com.atguigu.gulimall.search.vo.SearchResult;

/**
 * @Classname MallSearchService
 * @Description TODO
 * @Date 2021/8/11 1:26 下午
 * @Created by tangyao
 */

public interface MallSearchService {

    SearchResult search(SearchParam searchParam);
}
