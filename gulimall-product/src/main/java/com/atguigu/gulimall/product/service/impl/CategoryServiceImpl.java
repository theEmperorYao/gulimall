package com.atguigu.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.gulimall.product.dao.CategoryBrandRelationDao;
import com.atguigu.gulimall.product.service.CategoryBrandRelationService;
import com.atguigu.gulimall.product.vo.web.Catalog2Vo;
import org.apache.commons.lang.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;
    @Autowired
    private CategoryBrandRelationDao categoryBrandRelationDao;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private Redisson redisson;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        List<CategoryEntity> entityList = baseMapper.selectList(null);
        List<CategoryEntity> entities = entityList.stream()
                .filter(categoryEntity -> categoryEntity.getCatLevel() == 1)
                .map(menu -> {
                    menu.setChildren(getChildens(menu, entityList));
                    return menu;
                })
                .sorted(Comparator.comparingInt(item -> (item.getSort() == null ? 0 : item.getSort())))
                .collect(Collectors.toList());
        return entities;
    }

    @Override
    public void removeMenuByIds(List<Long> asList) {
        //TODO
        //1.检查当前删除菜单是否被别的地方引用
        baseMapper.deleteBatchIds(asList);
    }

    @Override
    public Long[] findCatelogPath(Long catelogId) {
//        CategoryEntity byId = this.getById(catelogId);
        CategoryEntity categoryEntity = baseMapper.selectById(catelogId);
        List<Long> path = new ArrayList<>();
        path.add(categoryEntity.getCatId());
        while (categoryEntity.getParentCid() != 0) {
            path.add(categoryEntity.getParentCid());
            categoryEntity = baseMapper.selectById(categoryEntity.getParentCid());
        }
        Collections.reverse(path);
        return path.toArray(new Long[path.size()]);
    }

    /**
     * 级联数据的更新，
     *
     * @param category
     * @CacheEvict：失效模式
     */


//    @Caching(evict={
//            @CacheEvict(value = {"catagory"}, key = "'getLevel1Categorys'"),
//            @CacheEvict(value = {"catagory"}, key = "'getCatalogJson'")
//    })
    @CacheEvict(value = {"catagory"}, allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationDao.updateCategory(category.getCatId(), category.getName());
        //同时修改缓存中的数据，
        //redis.del('catalogJSON'),等待下次查询时更新
    }

    @Override
    public List<CategoryEntity> listShowWithTree() {
        //1.按照排序查询出所有一级分类
        //2.设置每一个一级分类下所有的二级分类以及二级分类对应的三级分类,组装父子的树形

        List<CategoryEntity> categoryEntities = this.getBaseMapper().selectList(null);
        List<CategoryEntity> collect = categoryEntities.stream()
                .filter(categoryEntity -> categoryEntity.getCatLevel() == 1)
                .map(categoryEntity -> {
                    categoryEntity.setChildren(getChildren(categoryEntity, categoryEntities));
                    return categoryEntity;
                })
                .sorted(Comparator.comparingInt(item -> Optional.ofNullable(item.getSort()).orElse(0)))
                .collect(Collectors.toList());
        return collect;
    }


    /**
     * 每一个缓存的数据我们都来制定来放到哪个名字的缓存【缓存的分区（安装业务类型分）】
     * <p>
     * 代表当前方法的结果需要缓存,如果缓存中有，方法不用调用。
     * 如果缓存中没有，会调用方法，最后将方法的结果放入缓存
     * <p>
     * <p>
     * 3、默认行为
     * 1）、如果缓存中有，方法不用调用
     * 2）、key默认自动生成，缓存名字::SimpleKey []（自动生成的key）
     * 3）、缓存的value值，默认使用的是jdk的序列化机制，将序列化后的值存在redis中
     * 4）、默认时间ttl=-1
     * <p>
     * 自定义属性：
     * 1）、指定生成的缓存使用的key：key属性指定，使用spel表达式
     * SPEL表达式：https://docs.spring.io/spring/docs/5.2.7.RELEASE/spring-framework-reference/integration.html#cache-spel-context
     * 2）、指定缓存的数据的存活时间：配置文件中修改ttl，spring.cache.redis.time-to-live=3600000
     * 3）、将数据保存为json格式
     *
     * @return
     */

    @Cacheable(value = {"catagory"}, key = "#root.method.name", sync = true)
    @Override
    public List<CategoryEntity> getLevel1Categorys() {
        System.out.println("getLevel1Categorys......");
        long l = System.currentTimeMillis();
        List<CategoryEntity> categoryEntities = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
        return categoryEntities;
//        return null;
    }

//    @Cacheable(value = {"catagory"}, key = "#root.method.name")
    @Override
    public Map<String, List<Catalog2Vo>> getCatalogJson() {

        System.out.println(Thread.currentThread().getName() + "查询了数据库");
        List<CategoryEntity> selectList = baseMapper.selectList(null);

        List<CategoryEntity> category = getParent_cid(selectList, 0L);

        //2、封装数据
        Map<String, List<Catalog2Vo>> catelogMap = category.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            //1、查到这个一级分类下的所有二级分类
            List<CategoryEntity> categoryEntities = getParent_cid(selectList, v.getCatId());

            //2、封装上面的结果
            List<Catalog2Vo> catalog2Vos = new ArrayList<>();
            if (categoryEntities != null && categoryEntities.size() != 0) {
                catalog2Vos = categoryEntities.stream().map(l2 -> {
                    Catalog2Vo catalog2Vo = new Catalog2Vo(
                            l2.getParentCid().toString(), null, l2.getCatId().toString(), l2.getName());
                    //1、找当前二级分类的三级分类封装成vo
                    List<CategoryEntity> level3Catalog = getParent_cid(selectList, l2.getCatId());
                    List<Catalog2Vo.Catalog3Vo> collectlevel3 = new ArrayList<>();
                    if (level3Catalog != null && !level3Catalog.isEmpty()) {
                        //2、封装成指定格式
                        collectlevel3 = level3Catalog.stream().map(l3 -> {
                            Catalog2Vo.Catalog3Vo catalog3Vo = new Catalog2Vo.Catalog3Vo(l3.getParentCid().toString(),
                                    l3.getCatId().toString(), l3.getName());
                            return catalog3Vo;
                        }).collect(Collectors.toList());
                    }
                    catalog2Vo.setCatalog3List(collectlevel3);
                    return catalog2Vo;
                }).collect(Collectors.toList());
            }
            return catalog2Vos;
        }));

        return catelogMap;

    }


    /**
     * TODO: 产生堆外内存溢出OutOfDirectMermoryError
     * 1、)Springboot2.0以后，默认使用lettuce作为连接redis的客户端，他使用netty进行网络通讯。
     * 2、)lettuce的bug，导致netty堆外内存溢出，-Xmx300m,如果没有指定堆外内存，默认会使用-Xmx300m作为堆外内存，
     *
     * @return
     */
    public Map<String, List<Catalog2Vo>> getCatalogJson2() {
        //给缓存中放json字符串，拿出json字符串，还能逆转为能用的对象类型，【序列化与反序列化】

        /**
         * 1、空结果缓存，解决缓存穿透
         * 2、设置过期时间（加随机值），解决缓存雪崩
         * 3、加锁，解决缓存击穿
         *
         */

        //1、加入缓存逻辑，缓存中存储的是json字符串。
        //JSON跨语言，跨平台兼容
        String catalogJSON = stringRedisTemplate.opsForValue().get("catalogJSON");
        if (StringUtils.isEmpty(catalogJSON)) {
            //2、缓存中没有，查询数据库
            System.out.println("缓存不命中。。。。将要查询数据库。。。。");
            Map<String, List<Catalog2Vo>> catalogJsonFromDb = getCatalogJsonFromDbWithRedissonLock();


            return catalogJsonFromDb;
        }
        System.out.println("缓存命中。。。。直接返回。。。。");
        //转为我们指定的对象
        Map<String, List<Catalog2Vo>> result = JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catalog2Vo>>>() {
        });

        return result;

    }

    /**
     * 缓存里面的数据如何和数据库保持一致，
     * 缓存数据一致性问题，
     * 1)、双写模式
     * 2)、失效模式
     *
     * @return
     */
    public Map<String, List<Catalog2Vo>> getCatalogJsonFromDbWithRedissonLock() {
        //锁的名字。锁的粒度，越细越快。
        //锁的粒度：具体缓存的是哪个数据，11-号商品 product-11-lock
        RLock lock = redisson.getLock("CatalogJson-lock");
        lock.lock();
        Map<String, List<Catalog2Vo>> dataFromDB = new HashMap<>();
        try {
            dataFromDB = getDataFromDB();
        } catch (Exception e) {

        } finally {
            lock.unlock();
        }
        return dataFromDB;

    }

    public Map<String, List<Catalog2Vo>> getCatalogJsonFromDbWithRedisLock() {

        String token = UUID.randomUUID().toString();

        //1、占分布式锁。去redis占坑，
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent("lock", token, 100, TimeUnit.SECONDS);


        if (lock) {
            System.out.println("获取分布式锁成功");

            //为了让锁自动续期，不至于执行途中因为时间过短而失效，可以设置时间长一些，然后finally保证业务操作完成之后，就执行删除锁的操作
            //不管怎样，哪怕崩溃也直接解锁，不关心业务异常
            Map<String, List<Catalog2Vo>> dataFromDB;
            try {
                //加锁成功
                dataFromDB = getDataFromDB();
            } finally {

                //存在网络时延问题，比如在redis获取到lock返回时，lock过期被自动删除，
                // 此时其他线程抢占了锁，创建了lock，但是会被这个线程删掉的情况
//            String lock1 = stringRedisTemplate.opsForValue().get("lock");
//            if (token.equals(lock1)) {
//                stringRedisTemplate.delete("lock");
//            }
                String lua = "if redis.call(\"get\",KEYS[1]) == ARGV[1]\n" +
                        "then\n" +
                        "    return redis.call(\"del\",KEYS[1])\n" +
                        "else\n" +
                        "    return 0\n" +
                        "end";
                //RedisScript<T> script, List<K> keys, Object... args
                RedisScript<Long> luaScript = RedisScript.of(lua, Long.class);
                //删除锁
                Long lock1 = stringRedisTemplate.execute(luaScript, Arrays.asList("lock"), token);
            }

            return dataFromDB;
        } else {
            System.out.println("获取分布式锁失败，等待重试");
            //加锁失败。。。重试      synchronized
            //自旋的方式
            //休眠100ms重试
            try {
                TimeUnit.MILLISECONDS.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return getCatalogJsonFromDbWithRedisLock();
        }

    }

    private Map<String, List<Catalog2Vo>> getDataFromDB() {
        //得到锁以后应该再去缓存中确定一次，如果没有才需要继续查询
        String catalogJSON = stringRedisTemplate.opsForValue().get("catalogJSON");
        if (StringUtils.isNotEmpty(catalogJSON)) {
            //缓存不为null,直接返回，
            Map<String, List<Catalog2Vo>> result = JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catalog2Vo>>>() {
            });

            return result;
        }
        System.out.println(Thread.currentThread().getName() + "查询了数据库");
        List<CategoryEntity> selectList = baseMapper.selectList(null);

        List<CategoryEntity> category = getParent_cid(selectList, 0L);

        //2、封装数据
        Map<String, List<Catalog2Vo>> parent_cid = category.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            //1、查到这个一级分类下的所有二级分类
            List<CategoryEntity> categoryEntities = getParent_cid(selectList, v.getCatId());

            //2、封装上面的结果
            List<Catalog2Vo> catalog2Vos = new ArrayList<>();
            if (categoryEntities != null && categoryEntities.size() != 0) {
                catalog2Vos = categoryEntities.stream().map(l2 -> {
                    Catalog2Vo catalog2Vo = new Catalog2Vo(
                            l2.getParentCid().toString(), null, l2.getCatId().toString(), l2.getName());
                    //1、找当前二级分类的三级分类封装成vo
                    List<CategoryEntity> level3Catalog = getParent_cid(selectList, l2.getCatId());
                    List<Catalog2Vo.Catalog3Vo> collectlevel3 = new ArrayList<>();
                    if (level3Catalog != null && !level3Catalog.isEmpty()) {
                        //2、封装成指定格式
                        collectlevel3 = level3Catalog.stream().map(l3 -> {
                            Catalog2Vo.Catalog3Vo catalog3Vo = new Catalog2Vo.Catalog3Vo(l3.getParentCid().toString(),
                                    l3.getCatId().toString(), l3.getName());
                            return catalog3Vo;
                        }).collect(Collectors.toList());
                    }
                    catalog2Vo.setCatalog3List(collectlevel3);
                    return catalog2Vo;
                }).collect(Collectors.toList());
            }
            return catalog2Vos;
        }));

        //3、将查到的数据放到缓存,将对象转为json放到缓存中
        String s = JSON.toJSONString(parent_cid);
        stringRedisTemplate.opsForValue().set("catalogJSON", s, 1, TimeUnit.DAYS);


        return parent_cid;
    }


    //从数据库查询并封装分类数据
    public Map<String, List<Catalog2Vo>> getCatalogJsonFromDbWithLocalLock() {


        //只要是同一把锁，就能锁住需要这个锁的所有线程
        //1、synchronized (this)：Springboot所有的组件在容器中都是单例的，所以即使有100万并发进来，
        // 调CategoryServiceImpl的这个方法，这个service只有一个实例对象，this是单例的，相当于100个请求用的是同一个this，就能锁住了

        //todo 本地锁，synchronized，JUC（lock）在分布式情况下，想要锁住所有，必须使用分布式锁，
        synchronized (this) {

            //得到锁以后应该再去缓存中确定一次，如果没有才需要继续查询
            return getDataFromDB();
        }
    }

    private List<CategoryEntity> getParent_cid(List<CategoryEntity> selectList, Long Parent_cid) {
        List<CategoryEntity> collect =
                selectList.stream().filter(item -> item.getParentCid().equals(Parent_cid)).collect(Collectors.toList());

        return collect;
//        return baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq(
//                "parent_cid", v.getCatId()));
    }


    /**
     * @param categoryEntity   当前遍历中的每一个分类
     * @param categoryEntities 所有的分类结果
     * @return
     */
    private List<CategoryEntity> getChildren(CategoryEntity categoryEntity, List<CategoryEntity> categoryEntities) {
        List<CategoryEntity> collect = categoryEntities.stream()
                .filter(item -> item.getParentCid().equals(categoryEntity.getCatId()))
                .map(item -> {
                    item.setChildren(getChildren(item, categoryEntities));
                    return item;
                })
                .sorted(Comparator.comparing(item -> item.getSort() == null ? 0 : item.getSort()))
                .collect(Collectors.toList());
        return collect;
    }

    /**
     * 查找所有菜单的子菜单
     *
     * @param root
     * @param all
     * @return
     */
    private List<CategoryEntity> getChildens(CategoryEntity root, List<CategoryEntity> all) {
        List<CategoryEntity> childList = all.stream()
                .filter(item -> item.getParentCid().equals(root.getCatId()))
                .map(menu -> {
                    menu.setChildren(getChildens(menu, all));
                    return menu;
                })
                .sorted(Comparator.comparingInt(item -> (item.getSort() == null ? 0 : item.getSort())))
                .collect(Collectors.toList());
        return childList;
    }


}