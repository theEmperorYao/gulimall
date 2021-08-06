package com.atguigu.gulimall.product.app;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


import com.atguigu.gulimall.product.entity.ProductAttrValueEntity;
import com.atguigu.gulimall.product.service.ProductAttrValueService;
import com.atguigu.gulimall.product.vo.AttrRespVO;
import com.atguigu.gulimall.product.vo.AttrVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.atguigu.gulimall.product.service.AttrService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;


/**
 * 商品属性
 *
 * @author tangyao
 * @email 1013654193@gmail.com
 * @date 2020-05-15 01:37:34
 */
@RestController
@RequestMapping("product/attr")
public class AttrController {
    @Autowired
    private AttrService attrService;
    @Autowired
    ProductAttrValueService productAttrValueService;


    /**
     * //    /product/attr/base/listforspu/{spuId}
     * 获取spu规格
     *
     * @return
     */
    @GetMapping("/base/listforspu/{spuId}")
    public R baseAttrListForSpu(@PathVariable("spuId") Long spuId) {
        List<ProductAttrValueEntity> page = productAttrValueService.baseAttrListForSpu(spuId);
        return R.ok().put("data", page);
    }

    ///product/attr/base/list/{catelogId}
//    @GetMapping("/{type}/list/{catelogId}")
//    public R baseAttrList(@RequestParam Map<String, Object> params,
//                          @PathVariable("type") String type,
//                          @PathVariable("catelogId") Long catelogId) {
//        PageUtils page = attrService.queryBaseAttrPage(params, type, catelogId);
//        return R.ok().put("page", page);
//    }

    @GetMapping("/{type}/list/{catelogId}")
    public R baseAttrList(@RequestParam Map<String, Object> params, @PathVariable String type,
                          @PathVariable Long catelogId) {
        PageUtils page = attrService.queryBaseAttrsPage(params, type, catelogId);
        return R.ok().put("page", page);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("product:attr:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = attrService.queryPage(params);

        return R.ok().put("page", page);
    }


    //    /**
//     * 信息
//     */
//    @RequestMapping("/info/{attrId}")
//    //@RequiresPermissions("product:attr:info")
//    public R info(@PathVariable("attrId") Long attrId) {
////        AttrEntity attr = attrService.getById(attrId);
//        AttrRespVO respVO = attrService.getAttrInfo(attrId);
//        return R.ok().put("attr", respVO);
//    }
    @GetMapping("/info/{attrId}")
    public R info(@PathVariable Long attrId) {
        AttrRespVO attrRespVO = attrService.getAttrsInfo(attrId);
        return R.ok().put("attr", attrRespVO);
    }


//    /**
//     * 保存
//     */
//    @RequestMapping("/save")
//    //@RequiresPermissions("product:attr:save")
//    public R save(@RequestBody AttrVO attr) {
//        attrService.saveAttr(attr);
//        return R.ok();
//    }

    /**
     * 保存
     */
    @PostMapping("/save")
    //@RequiresPermissions("product:attr:save")
    public R save(@RequestBody AttrVO attrVO) {
        attrService.saveDetail(attrVO);
        return R.ok();
    }


        /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:attr:update")
    public R update(@RequestBody AttrRespVO attr) {
//        attrService.updateById(attr);
        attrService.updateAttr(attr);

        return R.ok();
    }


    /**
     * /product/attr/update/{spuId}
     */
    @PostMapping("/update/{spuId}")
    public R updateSpuAttr(@PathVariable("spuId") Long spuId,
                           @RequestBody List<ProductAttrValueEntity> productAttrValueEntities) {
        productAttrValueService.updateSpuAttr(spuId, productAttrValueEntities);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:attr:delete")
    public R delete(@RequestBody Long[] attrIds) {
        attrService.removeByIds(Arrays.asList(attrIds));

        return R.ok();
    }

}
