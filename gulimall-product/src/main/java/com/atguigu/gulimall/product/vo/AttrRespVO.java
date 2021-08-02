package com.atguigu.gulimall.product.vo;

import lombok.Data;

/**
 * @author tangyao
 * @version 1.0.0
 * @Description TODO
 * @createTime 2020年09月01日 15:25:00
 */
@Data
public class AttrRespVO extends AttrVO {
    /**
     * "catelogName": "手机/数码/手机", //所属分类名字
     * 			"groupName": "主体", //所属分组名字
     */
    private String catelogName;
    private String groupName;
    private Long[] catelogPath;
}
