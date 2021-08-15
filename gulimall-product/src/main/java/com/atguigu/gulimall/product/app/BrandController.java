package com.atguigu.gulimall.product.app;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


import com.atguigu.common.valid.AddGroup;
import com.atguigu.common.valid.UpdateGroup;
import com.atguigu.common.valid.UpdateStatusGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.atguigu.gulimall.product.entity.BrandEntity;
import com.atguigu.gulimall.product.service.BrandService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;


/**
 * 品牌
 *
 * @author tangyao
 * @email 1013654193@gmail.com
 * @date 2020-05-15 01:37:34
 */
@RestController
@RequestMapping("product/brand")
public class BrandController {
    @Autowired
    private BrandService brandService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("product:brand:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = brandService.queryPage(params);
        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{brandId}")
    //@RequiresPermissions("product:brand:info")
    public R info(@PathVariable("brandId") Long brandId) {
        BrandEntity brand = brandService.getById(brandId);

        return R.ok().put("brand", brand);
    }


    /**
     * 信息
     */
    @RequestMapping("/infos")
    public R info(@RequestParam("brandIds") List<Long> brandIds) {
        List<BrandEntity> brand = brandService.getBrandsByIds(brandIds);

        return R.ok().put("brand", brand);
    }


    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:brand:save")
    public R save(@Validated({AddGroup.class}) @RequestBody BrandEntity brand /** , BindingResult result*/) {

//        if (result.hasErrors()){
//            List<FieldError> fieldErrors = result.getFieldErrors();
//            Map<String,Object> map=new HashMap<>();
//            fieldErrors.forEach(item->{
//                String field = item.getField();
//                String defaultMessage = item.getDefaultMessage();
//                map.put(field,defaultMessage);
//            });
//            return R.error(400,"提交的数据不合法").put("data",map);
//        }
        brandService.save(brand);
        return R.ok();
    }

    //    /**
//     * 修改
//     */
//    @RequestMapping("/update")
//    //@RequiresPermissions("product:brand:update")
//    public R update(@Validated({UpdateGroup.class}) @RequestBody BrandEntity brand){
////		brandService.updateById(brand);
//		brandService.updateDetail(brand);
//        return R.ok();
//    }
    @RequestMapping("/update")
    public R Update(@RequestBody @Validated({UpdateGroup.class}) BrandEntity brandEntity) {
        brandService.updateInfo(brandEntity);
        return R.ok();
    }


    /**
     * 修改状态
     */
    @RequestMapping("/update/status")
    //@RequiresPermissions("product:brand:update")
    public R updateStatus(@Validated({UpdateStatusGroup.class}) @RequestBody BrandEntity brand) {
        brandService.updateById(brand);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:brand:delete")
    public R delete(@RequestBody Long[] brandIds) {
        brandService.removeByIds(Arrays.asList(brandIds));

        return R.ok();
    }

}
