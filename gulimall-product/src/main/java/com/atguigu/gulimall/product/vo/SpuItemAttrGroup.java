package com.atguigu.gulimall.product.vo;

import lombok.Data;

import java.util.List;

/**
 * @Classname SpuItemAttrGroup
 * @Description TODO
 * @Date 2021/8/17 5:57 下午
 * @Created by tangyao
 */
@Data
public class SpuItemAttrGroup{
    private String groupName;
    private List<SpuBaseAttrVo> attrs;
}
