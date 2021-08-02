package com.atguigu.gulimall.search.controller;

import com.atguigu.common.exception.BizCodeEnum;
import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

/**
 * @author tangyao
 * @version 1.0.0
 * @Description TODO
 * @createTime 2020年10月17日 14:25:00
 */
@Slf4j
@RestController()
@RequestMapping("/search/save")
public class ElasticSearchSaveController {
    @Autowired
    ProductSaveService productSaveService;

    /**
     * 上架商品
     *
     * @param skuEsModels
     * @return
     */
    @PostMapping("/product")
    public R productStatusUp(@RequestBody List<SkuEsModel> skuEsModels) {
        Boolean b = false;
        try {
            //出现这种错误某一个sku数据真的有问题了
            b = productSaveService.productStatusUp(skuEsModels);
        } catch (IOException e) {
            //出现这种错误可能es客户端连不上了
            log.error("ElasticSearchSaveController商品上架错误，错误原因为{}", e);
            return R.error(BizCodeEnum.PRODUCT_UP_EXCEPTION.getCode(), BizCodeEnum.PRODUCT_UP_EXCEPTION.getMessage());
        }
        if (!b) {
            return R.ok();
        }
        return R.error(BizCodeEnum.PRODUCT_UP_EXCEPTION.getCode(), BizCodeEnum.PRODUCT_UP_EXCEPTION.getMessage());
    }

}
