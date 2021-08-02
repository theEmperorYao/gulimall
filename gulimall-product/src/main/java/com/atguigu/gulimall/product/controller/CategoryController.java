package com.atguigu.gulimall.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


import com.atguigu.common.valid.AddEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;



/**
 * 商品三级分类
 *
 * @author tangyao
 * @email 1013654193@gmail.com
 * @date 2020-05-15 01:37:34
 */
@RestController
@RequestMapping("product/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 列表
     */
//    @RequestMapping("/list/tree")
//    public R listWithTree(){
//        List<CategoryEntity>  categoryEntities= categoryService.listWithTree();
//        return R.ok().put("data", categoryEntities);
//    }

    @RequestMapping("/list/tree")
    public R listWithTree(){
        List<CategoryEntity>  categoryEntities= categoryService.listShowWithTree();
        return R.ok().put("data", categoryEntities);
    }




    /**
     * 信息
     */
    @RequestMapping("/info/{catId}")
    //@RequiresPermissions("product:category:info")
    public R info(@PathVariable("catId") Long catId){
		CategoryEntity category = categoryService.getById(catId);

        return R.ok().put("data", category);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:category:save")
    public R save(@RequestBody  CategoryEntity category){
		categoryService.save(category);

        return R.ok();
    }


    /**
     * 修改
     */
    @RequestMapping("/update/sort")
    //@RequiresPermissions("product:category:update")
    public R updateSort(@RequestBody List<CategoryEntity> categoryList){
        categoryService.updateBatchById(categoryList);
        return R.ok();
    }

//    /**
//     * 修改
//     */
//    @RequestMapping("/update")
//    //@RequiresPermissions("product:category:update")
//    public R update(@RequestBody CategoryEntity category){
////		categoryService.updateById(category);
//        categoryService.updateCascade(category);
//        return R.ok();
//    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:category:update")
    public R update(@RequestBody CategoryEntity category){
		categoryService.updateById(category);
//        categoryService.updateCascade2(category);
        return R.ok();
    }

    /**
     * 删除
     * @RequestBody 获取请求体，必须发送post请求
     * SpringMVC会自动将请求体的数据(json),转化为对应对象
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:category:delete")
    public R delete(@RequestBody Long[] catIds){
//        categoryService.removeByIds(Arrays.asList(catIds));
        categoryService.removeMenuByIds(Arrays.asList(catIds));
        return R.ok();
    }

}
