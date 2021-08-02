package com.atguigu.gulimall.product.entity;

import com.atguigu.common.valid.*;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;

import lombok.Data;
import org.apache.ibatis.annotations.Update;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.*;

/**
 * 品牌
 *
 * @author tangyao
 * @email 1013654193@gmail.com
 * @date 2020-05-15 00:43:18
 */
@Data
@TableName("pms_brand")
public class BrandEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 品牌id
     */
    @NotNull(message = "修改品牌id不能为空", groups = {UpdateGroup.class,UpdateStatusGroup.class})
    @Null(message = "新增品牌id必须为空", groups = {AddGroup.class})
    @TableId
    private Long brandId;
    /**
     * 品牌名
     *  这里用@NotBlank，表示该字符串必须要有实际内容且不能是空字符串“  ”
     */
    @NotBlank(message = "品牌名必须提交",groups = {AddGroup.class, UpdateGroup.class})
    private String name;
    /**
     * 品牌logo地址
     */
    @NotBlank(groups = {AddGroup.class})
    @URL(message = "logo必须是一个合法的url地址", groups = {AddGroup.class, UpdateGroup.class})
    private String logo;
    /**
     * 介绍
     */
    private String descript;
    /**
     * 显示状态[0-不显示；1-显示]
     */
//    @NotNull(groups = {AddGroup.class, UpdateStatusGroup.class})
//    @ListValue(groups = {AddGroup.class   , UpdateStatusGroup.class, UpdateGroup.class})
    @NotNull(groups = {AddGroup.class})
    @OnlyOneOrEero(groups = {AddGroup.class, UpdateGroup.class,UpdateStatusGroup.class})
    private Integer showStatus;
    /**
     * 检索首字母
     * 正则表达式在这里不需要加"/^[a-zA-Z]$/"
     * 这里用@NotEmpty表示，不能为null也不能空，就算为空字符串，“  ”，因为下面的 @Pattern会进一步检验字符串内容为一个字母
     */
    @NotEmpty(groups = {AddGroup.class})
    @Pattern(regexp = "^[a-zA-Z]$", message = "检索首字母必须是一个字母", groups = {AddGroup.class, UpdateGroup.class})
    private String firstLetter;
    /**
     * 排序
     */
    @NotNull(groups = {AddGroup.class})
    @Min(value = 0, message = "排序必须大于等于0", groups = {AddGroup.class, UpdateGroup.class})
    private Integer sort;

}
