package com.atguigu.gulimall.product.entity;

import com.atguigu.common.valid.AddEntity;
import com.atguigu.common.valid.OnlyOneOrEero;
import com.atguigu.common.valid.UpdateEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;

/**
 * 属性分组
 * 
 * @author tangyao
 * @email 1013654193@gmail.com
 * @date 2020-05-15 00:43:18
 */
@Data
@TableName("pms_attr_group")
public class AttrGroupEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 分组id
	 */
	@TableId
	@NotNull(message = "修改时id不能为空",groups = {UpdateEntity.class})
	private Long attrGroupId;
	/**
	 * 组名
	 */
	@NotBlank(message = "组名不能为空",groups = {UpdateEntity.class, AddEntity.class})
	private String attrGroupName;
	/**
	 * 排序
	 */
	@PositiveOrZero(message = "排序必须是大于等于0的数",groups={UpdateEntity.class, AddEntity.class})
	private Integer sort;
	/**
	 * 描述
	 */
	@NotBlank(message = "描述不能为空",groups = {UpdateEntity.class, AddEntity.class})
	private String descript;
	/**
	 * 组图标
	 */
	@NotBlank(message = "组图标不能为空",groups = {UpdateEntity.class, AddEntity.class})
	private String icon;
	/**
	 * 所属分类id
	 */
	@NotNull(message = "三级分类id不能为空",groups = {UpdateEntity.class,AddEntity.class})
	@PositiveOrZero(groups={UpdateEntity.class, AddEntity.class})
	private Long catelogId;

	/**
	 * 完整路径
	 */
	@TableField(exist = false)
	private Long[] catelogPath;

}
