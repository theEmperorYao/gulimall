package com.atguigu.gulimall.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.constant.ProductConstant;
import com.atguigu.common.to.SkuReductionTo;
import com.atguigu.common.to.SpuBoundTo;
import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.dao.SpuInfoDao;
import com.atguigu.gulimall.product.entity.*;
import com.atguigu.gulimall.product.feign.CouponFeignService;
import com.atguigu.gulimall.product.feign.SearchFeignService;
import com.atguigu.gulimall.product.feign.WareFeignService;
import com.atguigu.gulimall.product.service.*;
import com.atguigu.gulimall.product.vo.*;
import com.atguigu.gulimall.product.vo.generated.SpuSaveVo2;
import com.atguigu.gulimall.product.vo.ware.SpuHasStockVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    private SpuImagesService spuImagesService;

    @Autowired
    private SpuInfoDescService spuInfoDescService;
    @Autowired
    private ProductAttrValueService productAttrValueService;

    @Autowired
    private SkuInfoService skuInfoService;

    @Autowired
    private SkuImagesService skuImagesService;

    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    private CouponFeignService couponFeignService;

    @Autowired
    private AttrService attrService;

    @Autowired
    private WareFeignService wareFeignService;

    @Autowired
    private BrandService brandService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SearchFeignService searchFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * @description
     * todo 高级部分完善
     *     @GlobalTransactional
     *     Seata AT 分布式事务
     * @return void
     * @version V1.0.0
     * @date 4:21 下午 2021/10/6
     * @author tangyao
     */

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveSpuInfo(SpuSaveVo vo) {

        //1.保存spu基本信息 ->pms_spu_info
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(vo, spuInfoEntity);
        spuInfoEntity.setCreateTime(new Date());
        spuInfoEntity.setUpdateTime(new Date());
        this.saveBaseSpuInfo(spuInfoEntity);

        //2.保存spu的描述图片 pms_spu_info_desc
        List<String> decript = vo.getDecript();
        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        spuInfoDescEntity.setSpuId(spuInfoEntity.getId());
        spuInfoDescEntity.setDecript(String.join(",", decript));
        spuInfoDescService.save(spuInfoDescEntity);

        //3.保存spu的图片集 pms_spu_images
        List<String> images = vo.getImages();
        spuImagesService.saveImageInfo(spuInfoEntity.getId(), images);

        //4.保存spu的规格参数 pms_product_attr_value
        List<BaseAttrs> baseAttrs = vo.getBaseAttrs();
        productAttrValueService.saveBaseAttrsInfo(spuInfoEntity.getId(), baseAttrs);

        //5.保存spu的积分信息 gulimall_sms sms_spu_bounds

        Bounds bounds = vo.getBounds();
        SpuBoundTo spuBoundTo = new SpuBoundTo();
        BeanUtils.copyProperties(bounds, spuBoundTo);
        spuBoundTo.setSpuId(spuInfoEntity.getId());
        R r = couponFeignService.saveSpuBounds(spuBoundTo);
        if (r.getCode() != 0) {
            log.error("远程保存积分信息失败");
        }


        //5.保存当前对应的所有sku信息；
//        private List<Attr> attr;
//        private String skuName;
//        private BigDecimal price;
//        private String skuTitle;
//        private String skuSubtitle;
        List<Skus> skus = vo.getSkus();
        if (skus != null && skus.size() != 0) {

            skus.forEach(item -> {
                String defaultImage = "";
                for (Images image : item.getImages()) {
                    if (image.getDefaultImg() == 1) {
                        defaultImage = image.getImgUrl();
                    }
                }
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(item, skuInfoEntity);
                skuInfoEntity.setSpuId(spuInfoEntity.getId());
                skuInfoEntity.setBrandId(spuInfoEntity.getBrandId());
                skuInfoEntity.setCatalogId(spuInfoEntity.getCatalogId());
                skuInfoEntity.setSaleCount(0L);
                skuInfoEntity.setSkuDefaultImg(defaultImage);
                //5.1)、sku的基本信息 pms_sku_info
                skuInfoService.save(skuInfoEntity);

                Long skuId = skuInfoEntity.getSkuId();

                List<SkuImagesEntity> collect = item.getImages().stream().map(image -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setImgUrl(image.getImgUrl());
                    skuImagesEntity.setDefaultImg(image.getDefaultImg());
                    return skuImagesEntity;
                }).filter(skuImagesEntity -> {
                    return StringUtils.isNotEmpty(skuImagesEntity.getImgUrl());
                }).collect(Collectors.toList());
                //5.2)、sku的图片信息 pms_sku_images
                skuImagesService.saveBatch(collect);
                //TODO 没有图片路径的无需保存

                List<Attr> attr = item.getAttr();
                List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = attr.stream().map(a -> {
                    SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(a, skuSaleAttrValueEntity);
                    skuSaleAttrValueEntity.setSkuId(skuId);

                    return skuSaleAttrValueEntity;
                }).collect(Collectors.toList());
                //5.3)、sku的销售属性信息 pms_sku_sale_attr_value
                skuSaleAttrValueService.saveBatch(skuSaleAttrValueEntities);


                //5.4)、sku的优惠满减信息 gulimall_sms sms_sku_ladder sms_sku_full_reduction sms_member_price
                SkuReductionTo skuReductionTo = new SkuReductionTo();
                BeanUtils.copyProperties(item, skuReductionTo);
                skuReductionTo.setSkuId(skuId);
                //当满几件和满多少金额有实际意义，也就是大于0的时候，调用远程服务保存优惠与满减信息，因为优惠和满减是两张表，所以用||
                if (skuReductionTo.getFullCount() > 0 ||
                        skuReductionTo.getFullPrice().compareTo(new BigDecimal("0")) == 1) {
                    R r1 = couponFeignService.saveSkuReduction(skuReductionTo);
                    if (r1.getCode() != 0) {
                        log.error("远程保存优惠信息失败");
                    }
                }


            });
        }


    }

    @Override
    public PageUtils queryPageByCondiction(Map<String, Object> params) {
        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (StringUtils.isNotEmpty(key)) {
            //where status=1 and (id =2 or spu_name like xxx) 如果没有括号，则只满足最后一个条件都可以，
            //所以用这种内嵌的条件的查询方式
            wrapper.and(w -> {
                w.eq("id", key).or().like("spu_name", key);
            });
        }
        String catelogId = (String) params.get("catelogId");
        if (StringUtils.isNotEmpty(catelogId) && !"0".equalsIgnoreCase(catelogId)) {
            wrapper.eq("catalog_id", catelogId);

        }
        String status = (String) params.get("status");
        if (StringUtils.isNotEmpty(status)) {
            wrapper.eq("publish_status", status);
        }
        String brandId = (String) params.get("brandId");
        if (StringUtils.isNotEmpty(brandId) && !"0".equalsIgnoreCase(brandId)) {
            wrapper.eq("brand_id", brandId);
        }

        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveSpuInfo2(SpuSaveVo2 vo) {
        //1.保存spu基本信息  pms_spu_info
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(vo, spuInfoEntity);
        spuInfoEntity.setCreateTime(new Date());
        spuInfoEntity.setUpdateTime(new Date());
        this.save(spuInfoEntity);

        Long spuId = spuInfoEntity.getId();

        //2.spu 描述长图信息 pms_spu_info_desc
        List<String> decript = vo.getDecript();

        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        spuInfoDescEntity.setDecript(String.join(",", decript));
        spuInfoDescEntity.setSpuId(spuId);
        spuInfoDescService.save(spuInfoDescEntity);

        //3.spu 图片信息 pms_spu_images
        List<String> images = vo.getImages();
        if (images != null && images.size() != 0) {
            List<SpuImagesEntity> spuImagesEntities = images.stream().map(item -> {
                SpuImagesEntity spuImagesEntity = new SpuImagesEntity();
                spuImagesEntity.setImgUrl(item);
                spuImagesEntity.setSpuId(spuId);
//            spuImagesEntity.setDefaultImg();
                return spuImagesEntity;
            }).collect(Collectors.toList());
            spuImagesService.saveBatch(spuImagesEntities);
        }


        //4.用户积分信息 gulimall_sms sms_spu_bounds
        com.atguigu.gulimall.product.vo.generated.Bounds bounds = vo.getBounds();
        SpuBoundTo spuBoundTo = new SpuBoundTo();
        spuBoundTo.setSpuId(spuId);
        BeanUtils.copyProperties(bounds, spuBoundTo);
        couponFeignService.saveSpuBounds(spuBoundTo);

        //5.spu 基本属性的值 pms_product_attr_value
        List<com.atguigu.gulimall.product.vo.generated.BaseAttrs> baseAttrs = vo.getBaseAttrs();
        List<ProductAttrValueEntity> productAttrValueEntities = baseAttrs.stream().map(item -> {
            ProductAttrValueEntity productAttrValueEntity = new ProductAttrValueEntity();
            productAttrValueEntity.setSpuId(spuId);
            productAttrValueEntity.setAttrId(item.getAttrId());
            productAttrValueEntity.setAttrValue(item.getAttrValues());
            productAttrValueEntity.setQuickShow(item.getShowDesc());
            AttrEntity attrEntity = attrService.getById(item.getAttrId());
            if (attrEntity != null) {
                productAttrValueEntity.setAttrName(attrEntity.getAttrName());
            }
            return productAttrValueEntity;
        }).collect(Collectors.toList());
        productAttrValueService.saveBatch(productAttrValueEntities);

        //6.sku

        List<com.atguigu.gulimall.product.vo.generated.Skus> skus = vo.getSkus();
        if (skus != null && skus.size() != 0) {
            skus.forEach(sku -> {

                //获取默认图片
                List<com.atguigu.gulimall.product.vo.generated.Images> skuImages = sku.getImages();
                String defaultImage = "";
                List<com.atguigu.gulimall.product.vo.generated.Images> collect = skuImages.stream().filter(item -> item.getDefaultImg() == 1).collect(Collectors.toList());
                if (collect != null && collect.size() != 0) {
                    defaultImage = collect.get(0).getImgUrl();
                }
                //6.1)sku 基本信息  pms_sku_info
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                //     private String skuName;
                //    private BigDecimal price;
                //    private String skuTitle;
                //    private String skuSubtitle;
                BeanUtils.copyProperties(sku, skuInfoEntity);
                skuInfoEntity.setSpuId(spuId);
                skuInfoEntity.setCatalogId(spuInfoEntity.getCatalogId());
                skuInfoEntity.setBrandId(skuInfoEntity.getBrandId());
                skuInfoEntity.setSaleCount(0L);
                skuInfoEntity.setSkuDefaultImg(defaultImage);
                skuInfoService.save(skuInfoEntity);

                Long skuId = skuInfoEntity.getSkuId();

                //6.2)销售属性的值 pms_sku_sale_attr_value
                List<com.atguigu.gulimall.product.vo.generated.Attr> attr = sku.getAttr();
                List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = attr.stream().map(item -> {
                    SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(item, skuSaleAttrValueEntity);
                    skuSaleAttrValueEntity.setSkuId(skuId);
                    return skuSaleAttrValueEntity;
                }).collect(Collectors.toList());
                skuSaleAttrValueService.saveBatch(skuSaleAttrValueEntities);

                //6.3)sku 图片信息  pms_sku_info
                List<SkuImagesEntity> skuImagesEntities = skuImages.stream().map(item -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setDefaultImg(item.getDefaultImg());
                    skuImagesEntity.setImgUrl(item.getImgUrl());
                    return skuImagesEntity;
                }).filter(item -> StringUtils.isNotEmpty(item.getImgUrl())).collect(Collectors.toList());
                skuImagesService.saveBatch(skuImagesEntities);

                //6.4)sku 满减信息 满几件打几折，满多少减多少 gulimall_sms sms_sku_full_reduction sms_sku_ladder
                SkuReductionTo skuReductionTo = new SkuReductionTo();
                BeanUtils.copyProperties(sku, skuReductionTo);
                skuReductionTo.setSkuId(skuId);
                if (skuReductionTo.getFullCount() <= 0 || skuReductionTo.getFullPrice().compareTo(new BigDecimal(0)) == 1) {
                    R r = couponFeignService.saveSkuReductionInfo(skuReductionTo);
                    if (r.getCode() != 0) {
                        System.out.println("远程服务调用失败");
                    }
                }

            });

        }


        //6.5)sku 会员价格 gulimall_sms sms_member_price


    }

    @Override
    public void up(Long spuId) {

        //组装需要的数据
        //1、查出当前spuid对应的所有sku信息
        List<SkuInfoEntity> skus = skuInfoService.getSkusBySpuId(spuId);
        List<Long> skuIds = skus.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());

        //调用远程仓储服务，提前查出来，避免循环查库
        Map<Long, Boolean> stockMap = null;
        // todo 1、发送远程调用，库存系统查询是否有库存
        try {
            //会有8次库存查询，所以希望远程服务有一个接口可以统一帮我们查到所有sku有没有库存
            R r = wareFeignService.getSkuHasStock(skuIds);
            TypeReference<List<SpuHasStockVo>> typeReference = new TypeReference<List<SpuHasStockVo>>() {
            };
            stockMap = r.getData(typeReference).stream().collect(Collectors.toMap(SpuHasStockVo::getSkuId,
                    SpuHasStockVo::getStock));
        } catch (Exception e) {
            log.error("调用库存服务查询异常，原因为{}" + e);
        }

        //封装attrs
        //todo 4、查询当前sku所有可以被检索的规格属性
//        productAttrValueService.baseAttrListForSpu();
        List<ProductAttrValueEntity> attrsBySpuId = productAttrValueService.getAttrsBySpuId(spuId);
        List<Long> attrIds = attrsBySpuId.stream().map(ProductAttrValueEntity::getAttrId).collect(Collectors.toList());

        //这是可被检索属性的id集合
        List<Long> searchAttrIds = attrService.selectSearchAttrIds(attrIds);
        //为了筛选出可被检索的商品attrs
        Set<Long> setAttrIds = new HashSet<>(searchAttrIds);
        //拿到所有的可以被检索的商品属性关系表中数据，并提取出商品需要的attrs
        List<SkuEsModel.Attrs> attrsList = attrsBySpuId.stream()
                .filter(item -> setAttrIds.contains(item.getAttrId()))
                .map(item -> {
                    SkuEsModel.Attrs attrs = new SkuEsModel.Attrs();
                    BeanUtils.copyProperties(item, attrs);
                    return attrs;
                }).collect(Collectors.toList());

        //封装每个sku信息
        Map<Long, Boolean> finalStockMap = stockMap;
        List<SkuEsModel> upProducts = skus.stream().map(sku -> {
            SkuEsModel skuEsModel = new SkuEsModel();
            BeanUtils.copyProperties(sku, skuEsModel);

            // skuPrice skuImg
            skuEsModel.setSkuPrice(sku.getPrice());
            skuEsModel.setSkuImg(sku.getSkuDefaultImg());

            // hasStock hotScore
            if (finalStockMap == null) {
                skuEsModel.setHasStock(true);
            } else {
                skuEsModel.setHasStock(finalStockMap.get(sku.getSkuId()));
            }
            // todo 2、热度评分。0
            skuEsModel.setHotScore(0L);

            // brandName brandImg
            //todo 3、查询品牌和分类的名字信息
            BrandEntity brand = brandService.getById(sku.getBrandId());
            skuEsModel.setBrandName(brand.getName());
            skuEsModel.setBrandImg(brand.getLogo());
            //  catalogName
            CategoryEntity category = categoryService.getById(sku.getCatalogId());
            skuEsModel.setCatalogName(category.getName());

            //   private Long attrId;
            //   private String attrName;
            //   private String attrValue;
            //设置检索属性
            skuEsModel.setAttrs(attrsList);
            return skuEsModel;
        }).collect(Collectors.toList());
        // 远程调用上架商品
        //todo 5、将数据发送给es保存
        R r = searchFeignService.productStatusUp(upProducts);
        if (r.getCode() == 0) {
            //远程调用成功
            //todo 更改spuinfo中商品的发布状态为已上架
            //状态应该作为枚举类存在的
            baseMapper.updateSpuStatus(spuId, ProductConstant.StatusEnum.SPU_UP.getCode());
        } else {
            //远程调用失败
            //todo 7、重复调用？接口幂等性；重试机制？xxx
        }


    }

    @Override
    public SpuInfoEntity getSpuInfoBySkuId(Long skuId) {

        SkuInfoEntity byId = skuInfoService.getById(skuId);
        Long spuId = byId.getSpuId();
        SpuInfoEntity spuInfoEntity = getById(spuId);
        return spuInfoEntity;
    }

    private void saveBaseSpuInfo(SpuInfoEntity spuInfoEntity) {
        this.baseMapper.insert(spuInfoEntity);
    }

}