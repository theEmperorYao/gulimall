package com.atguigu.gulimall.product.app;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


import com.atguigu.common.valid.AddEntity;
import com.atguigu.gulimall.product.entity.AttrEntity;
import com.atguigu.gulimall.product.service.AttrAttrgroupRelationService;
import com.atguigu.gulimall.product.service.AttrService;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.AttrGroupRelationVO;
import com.atguigu.gulimall.product.vo.AttrGroupWithAttrsVO;
import com.atguigu.gulimall.product.vo.AttrRespVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.atguigu.gulimall.product.entity.AttrGroupEntity;
import com.atguigu.gulimall.product.service.AttrGroupService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;


/**
 * 属性分组
 *
 * @author tangyao
 * @email 1013654193@gmail.com
 * @date 2020-05-15 01:37:34
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private CategoryService categoryService;
    @Autowired
    private AttrAttrgroupRelationService relationService;

    @Autowired
    private AttrService attrService;

    /**
     * 获取分类下所有分组&关联属性
     * @param catelogId
     * @return
     */
//     /product/attrgroup/{catelogId}/withattr
//    @GetMapping("/{catelogId}/withattr")
//    public R getAttrgroupWithAttrs(@PathVariable("catelogId") Long catelogId) {
//        List<AttrGroupWithAttrsVO> attrGroupWithAttrsVOS = attrGroupService.getAttrgroupWithAttrsByCatelogId(catelogId);
//        return R.ok().put("data", attrGroupWithAttrsVOS);
//    }

    @GetMapping("/{catelogId}/withattr")
   public R getAttrGroupWithAttrs(@PathVariable Long catelogId){

        List<AttrGroupWithAttrsVO> attrGroupWithAttrsVOList=attrGroupService.getAttrGroupWithAttrs(catelogId);
        return R.ok().put("data",attrGroupWithAttrsVOList);
    }



    //    /product/attrgroup/attr/relation
    @PostMapping("/attr/relation")
    public R setRelation(@RequestBody List<AttrRespVO> attrRespVO) {
        relationService.setRelation(attrRespVO);
        return R.ok();
    }

//    //    /product/attrgroup/{attrgroupId}/noattr/relation
//    @GetMapping("{attrgroupId}/noattr/relation")
//    public R getOtherAttrs(@RequestParam Map<String, Object> params,
//                           @PathVariable("attrgroupId") int attrgroupId) {
//        PageUtils page = attrService.getOtherAttrs(params, attrgroupId);
//        return R.ok().put("page", page);
//    }
    ///11/noattr/relation

    @GetMapping("/{attrgroupId}/noattr/relation")
    public R getOtherAttrs2(@RequestParam Map<String, Object> params,@PathVariable Long attrgroupId){

        PageUtils page=attrService.getOtherAttrs2(params,attrgroupId);
        return R.ok().put("page",page);
    }


    /**
     * 删除分类与属性的关联关系
     * @param relationVOS
     * @return
     */
//    ///product/attrgroup/attr/relation/delete
//    @PostMapping("attr/relation/delete")
//    public R deleteRelation(@RequestBody AttrGroupRelationVO[] relationVOS) {
//        relationService.deleteRelation(relationVOS);
//        return R.ok();
//    }

    ///product/attrgroup/attr/relation/delete
    @PostMapping("attr/relation/delete")
    public R deleteRelation(@RequestBody List<AttrGroupRelationVO> relationVOS){
        relationService.deleteRelation2(relationVOS);
        return R.ok();
    }



//    //    /product/attrgroup/{attrgroupId}/attr/relation
//    @GetMapping("{attrgroupId}/attr/relation")
//    public R getAttrsRelation(@PathVariable("attrgroupId") Long attrgroupId) {
//        List<AttrEntity> entities = attrGroupService.getAttrsRelation(attrgroupId);
//        return R.ok().put("data", entities);
//    }

    @GetMapping("{attrgroupId}/attr/relation")
    public R getAttrRelation2(@PathVariable Long attrgroupId){
        List<AttrEntity> attrEntities=attrGroupService.getAttrsRelation2(attrgroupId);
        return R.ok().put("data",attrEntities);
    }
    /**
     * 列表
     */
//    @RequestMapping("/list/{catelogId}")
//    //@RequiresPermissions("product:attrgroup:list")
//    public R list(@RequestParam Map<String, Object> params, @PathVariable Long catelogId) {
////        PageUtils page = attrGroupService.queryPage(params);
//        PageUtils page = attrGroupService.queryPage(params, catelogId);
//        return R.ok().put("page", page);
//    }
    @RequestMapping("/list/{catelogId}")
    public R list(@RequestParam Map<String, Object> params, @PathVariable Long catelogId) {
        PageUtils page = attrGroupService.queryPageById(params, catelogId);
        return R.ok().put("page", page);
    }


    /**
     * 获取属性分组详情
     */
//    @RequestMapping("/info/{attrGroupId}")
//    //@RequiresPermissions("product:attrgroup:info")
//    public R info(@PathVariable("attrGroupId") Long attrGroupId) {
//        AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);
//        Long catelogId = attrGroup.getCatelogId();
//        Long[] path = categoryService.findCatelogPath(catelogId);
//        attrGroup.setCatelogPath(path);
//
//        return R.ok().put("attrGroup", attrGroup);
//    }
    @RequestMapping("/info/{attrGroupId}")
    public R info(@PathVariable Long attrGroupId) {
        AttrGroupEntity groupEntity = attrGroupService.getById(attrGroupId);
        List<Long> path = attrGroupService.findPath(groupEntity.getCatelogId());
        Long[] CatalogPath = path.toArray(new Long[0]);
        groupEntity.setCatelogPath(CatalogPath);
        return R.ok().put("attrGroup", groupEntity);
    }


    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:attrgroup:save")
    public R save(@RequestBody @Validated({AddEntity.class}) AttrGroupEntity attrGroup) {
        attrGroupService.save(attrGroup);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:attrgroup:update")
    public R update(@RequestBody AttrGroupEntity attrGroup) {
        attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:attrgroup:delete")
    public R delete(@RequestBody Long[] attrGroupIds) {
        attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }

}
