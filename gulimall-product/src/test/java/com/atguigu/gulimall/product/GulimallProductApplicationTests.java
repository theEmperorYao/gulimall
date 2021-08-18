package com.atguigu.gulimall.product;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.OSSClientBuilder;
import com.atguigu.common.to.SpuBoundTo;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.dao.AttrGroupDao;
import com.atguigu.gulimall.product.dao.SkuSaleAttrValueDao;
import com.atguigu.gulimall.product.entity.BrandEntity;
import com.atguigu.gulimall.product.feign.CouponFeignService;
import com.atguigu.gulimall.product.service.BrandService;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.ItemSaleAttrVo;
import com.atguigu.gulimall.product.vo.SpuItemAttrGroup;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@SpringBootTest
class GulimallProductApplicationTests {

    @Autowired
    BrandService brandService;

    @Autowired
    CategoryService categoryService;
    @Autowired
    CouponFeignService couponFeignService;

//    @Resource
//    OSS ossClient;

    @Autowired
    StringRedisTemplate stringRedisTemplate;


    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private AttrGroupDao attrGroupDao;

    @Autowired
    private SkuSaleAttrValueDao skuSaleAttrValueDao;

    @Test
    public void test() {
//        List<SpuItemAttrGroup> attrGroupWithAttrsBySpuId = attrGroupDao.getAttrGroupWithAttrsBySpuId(14L, 225L);
//        System.out.println("attrGroupWithAttrsBySpuId = " + attrGroupWithAttrsBySpuId);

        List<ItemSaleAttrVo> saleAttrsBuSpuId = skuSaleAttrValueDao.getSaleAttrsBuSpuId(14L);
        System.out.println("saleAttrsBuSpuId = " + saleAttrsBuSpuId);
    }


    @Test
    public void redisson() {
        System.out.println(redissonClient);
    }

    @Test
    public void testStringRedisTemplate() {
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();

        //保存
        ops.set("hello", "world_" + UUID.randomUUID().toString());

        //查询
        String hello = ops.get("hello");
        System.out.println("hello = " + hello);
    }


    @Test
    public void testUpload() throws FileNotFoundException {
        // Endpoint以杭州为例，其它Region请按实际情况填写。
//        String endpoint = "oss-cn-beijing.aliyuncs.com";
//        // 云账号AccessKey有所有API访问权限，建议遵循阿里云安全最佳实践，创建并使用RAM子账号进行API访问或日常运维，请登录 https://ram.console.aliyun.com 创建。
//        String accessKeyId = "LTAI4GBHjoyDKmGW9N1g1kZX";
//        String accessKeySecret = "eJvsz4XNSHyEqqfnSN442ySqxZsYyP";
//
//         //创建OSSClient实例。
//        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        // 上传文件流。
//        InputStream inputStream = new FileInputStream("C:\\Users\\Administrator\\Desktop\\思维导图\\谷粒商城问题提炼总结.xmind");
//        ossClient.putObject("gulimall-tangyao", "思维导图.xmind", inputStream);
//
//        // 关闭OSSClient。
//        ossClient.shutdown();
//        System.out.println("上传完成。。。");
    }

    @Test
    public void test02() {
        System.out.println("".length());
    }

    @Test
    public void test01() {
        SpuBoundTo spuBoundTo = new SpuBoundTo();
        spuBoundTo.setSpuId(996L);
        R r = couponFeignService.saveSpuBounds(spuBoundTo);
        System.out.println(r);
    }


    @Test
    void testFindCatelogPath() {
        Long[] catelogPath = categoryService.findCatelogPath(353L);
        log.info("完整路径{}", Arrays.asList(catelogPath));
    }

    @Test
    void contextLoads() {
        BrandEntity entity = new BrandEntity();
//        entity.setName("唐尧");
//        brandService.save(entity);
//        System.out.println("保存成功");

//        entity.setBrandId(1L);
//        entity.setDescript("学习学习");
//        entity.setName("奥特们");
//        brandService.updateById(entity);
//        BrandEntity entity1 = brandService.getOne(new QueryWrapper<BrandEntity>().eq("brand_id", 1));
//        System.out.println(entity1);

        brandService.list(new QueryWrapper<BrandEntity>().eq("brand_id", 1)).forEach(System.out::println);


    }

}
