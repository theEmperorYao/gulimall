package com.atguigu.gulimall.seckill.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @Classname SeckillSessionWithSkus
 * @Description TODO
 * @Date 2021/10/10 9:51 下午
 * @Created by tangyao
 */
@Data
public class SeckillSessionWithSkus {
    private Long id;
    /**
     * 场次名称
     */
    private String name;
    /**
     * 每日开始时间
     */
    private Date startTime;
    /**
     * 每日结束时间
     */
    private Date endTime;
    /**
     * 启用状态
     */
    private Integer status;
    /**
     * 创建时间
     */
    private Date createTime;


    private List<SeckillSkuVo> relationSkus;

}
