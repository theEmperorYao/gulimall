package com.atguigu.gulimall.ware.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author tangyao
 * @version 1.0.0
 * @Description TODO
 * @createTime 2020年09月26日 13:48:00
 */
@Data
public class PurchaseDoneVo {
    /**
     * 采购单id
     */
    @NotNull
    private Long id;
    /**
     * 完成/失败的需求详情
     */
    private List<PurchaseItem> items;
}
