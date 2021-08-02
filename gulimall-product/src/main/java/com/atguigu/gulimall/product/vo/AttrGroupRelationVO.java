package com.atguigu.gulimall.product.vo;

import lombok.Data;

/**
 * @author tangyao
 * @version 1.0.0
 * @Description TODO
 * @createTime 2020年09月03日 14:22:00
 */
@Data
public class AttrGroupRelationVO {
    //[{"attrId":1,"attrGroupId":2}]
    private Long attrId;
    private Long attrGroupId;
}
