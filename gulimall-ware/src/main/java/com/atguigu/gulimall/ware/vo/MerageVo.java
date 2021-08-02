package com.atguigu.gulimall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * @author tangyao
 * @version 1.0.0
 * @Description TODO
 * @createTime 2020年09月25日 20:34:00
 */
@Data
public class MerageVo {

    /**
     * 整单id 用包装类型，因为可能不提交,就是null
     */
    private Long purchaseId;
    /**
     * 合并项集合
     */
    private List<Long> items;
}
