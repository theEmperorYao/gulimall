package com.atguigu.gulimall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.exception.NoStockException;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.common.utils.R;
import com.atguigu.common.vo.MemberRespVo;
import com.atguigu.gulimall.order.constant.OrderConstant;
import com.atguigu.gulimall.order.dao.OrderDao;
import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.entity.OrderItemEntity;
import com.atguigu.gulimall.order.enume.OrderStatusEnum;
import com.atguigu.gulimall.order.feign.CartFeignService;
import com.atguigu.gulimall.order.feign.MemberFeignService;
import com.atguigu.gulimall.order.feign.ProductFeignService;
import com.atguigu.gulimall.order.feign.WmsFeignService;
import com.atguigu.gulimall.order.interceptor.LoginUserInterceptor;
import com.atguigu.gulimall.order.service.OrderItemService;
import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.to.OrderCreateTo;
import com.atguigu.gulimall.order.vo.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {


    private ThreadLocal<OrderSubmitVo> confirmVoThreadLocal = new ThreadLocal<>();

    @Autowired
    MemberFeignService memberFeignService;

    @Autowired
    CartFeignService cartFeignService;

    @Autowired
    ThreadPoolExecutor executor;
    @Autowired
    WmsFeignService wmsFeignService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    OrderItemService orderItemService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException {
        OrderConfirmVo confirmVo = new OrderConfirmVo();

        MemberRespVo memberRespVo = LoginUserInterceptor.loginUser.get();
        //获取之前的请求
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        System.out.println("主线程。。。" + Thread.currentThread().getId());
        CompletableFuture<Void> getAddressFuture = CompletableFuture.runAsync(() -> {
            // 1、远程查询所有的收货地址列表
            System.out.println("member线程。。。" + Thread.currentThread().getId());
            // 每一个线程都要共享之前的请求数据
            RequestContextHolder.setRequestAttributes(attributes);
            List<MemberAddressVo> address = memberFeignService.getAddress(memberRespVo.getId());
            confirmVo.setAddress(address);
        }, executor);

        CompletableFuture<Void> cartFuture = CompletableFuture.runAsync(() -> {
            // 2、远程查询购物车所有选中的购物项
            System.out.println("cart线程。。。" + Thread.currentThread().getId());
            // 每一个线程都要共享之前的请求数据
            RequestContextHolder.setRequestAttributes(attributes);
            List<OrderItemVo> items = cartFeignService.getCurrentUserCartItems();
            confirmVo.setItems(items);
            //feign 在远程调用之前要构造请求，调用很多拦截器
            // (RequestInterceptor interceptor : requestInterceptors)
        }, executor).thenRunAsync(() -> {
            List<OrderItemVo> items = confirmVo.getItems();
            List<Long> collect = items.stream().map(OrderItemVo::getSkuId).collect(Collectors.toList());
            R hasStock = wmsFeignService.getSkuHasStock(collect);

            List<SkuStockVo> data = hasStock.getData(new TypeReference<List<SkuStockVo>>() {
            });

            if (data != null) {
                Map<Long, Boolean> stocks = data.stream().collect(Collectors.toMap(SkuStockVo::getSkuId, SkuStockVo::getStock));
                confirmVo.setStocks(stocks);
            }

        }, executor);


        // 3、查询用户积分
        Integer integration = memberRespVo.getIntegration();
        confirmVo.setIntegration(integration);

        // 4、其他数据自动计算

        // todo 5、防重令牌
        String token = UUID.randomUUID().toString().replace("-", "");
        confirmVo.setOrderToken(token);
        stringRedisTemplate.opsForValue()
                .set(
                        OrderConstant.USER_ORDER_TOKEN_PREFIX + memberRespVo.getId(),
                        token,
                        30,
                        TimeUnit.MINUTES);

        CompletableFuture.allOf(getAddressFuture, cartFuture).get();

        return confirmVo;
    }

    // 同一个方法内事务方法互相调用默认失效（原因是绕过了代理对象）
    // 事务使用代理对象来控制的
    @Transactional(timeout = 30)//a事务的所有设置就传播到了和他公用的一个事务的方法
    public void a() {
        //b,c 做任何设置都没有，都是和a公用一个事务

        OrderServiceImpl orderService = (OrderServiceImpl) AopContext.currentProxy();
        orderService.b();
        orderService.c();
//        this.b();
//        this.c();
//        bService.b();//a事务
//        cService.c();//新事务(不回滚)
        int i = 10 / 0;
    }

    @Transactional(propagation = Propagation.REQUIRED,timeout = 2)
    public void b() {

    }

    @Transactional(propagation = Propagation.REQUIRES_NEW,timeout = 20)
    public void c() {

    }

    // 本地事务，在分布式系统，只能控制住自己的回滚，控制不了其他服务的回滚
    // 分布式事务：最大原因，网络问题+分布式机器。
    //(isolation = Isolation.REPEATABLE_READ )

//    @GlobalTransactional// 高并发不适合
    @Transactional
    @Override
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo orderSubmitVo) {
        confirmVoThreadLocal.set(orderSubmitVo);
        SubmitOrderResponseVo response = new SubmitOrderResponseVo();
        MemberRespVo memberRespVo = LoginUserInterceptor.loginUser.get();
        response.setCode(0);


        //1、验证令牌【令牌的对比和删除保证原子性】
        // 0 - 令牌校验失败，1 - 删除成功

        String lua = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";

        String orderToken = orderSubmitVo.getOrderToken();
        String key = OrderConstant.USER_ORDER_TOKEN_PREFIX + memberRespVo.getId();

        // 原子验证令牌和删除令牌
        Long result = stringRedisTemplate.execute(
                new DefaultRedisScript<Long>(lua, Long.class),
                Arrays.asList(key),
                orderToken);

        if (result == 0L) {
            // 令牌验证失败
            response.setCode(1);
            return response;
        } else {
            // 令牌验证成功
            // 下单：去创建订单，验令牌，验价格，锁库存
            //1、创建订单，订单项信息
            OrderCreateTo order = createOrder();
            //2、验价
            BigDecimal payAmount = order.getOrder().getPayAmount();
            BigDecimal payPrice = orderSubmitVo.getPayPrice();
            if (Math.abs(payAmount.subtract(payPrice).doubleValue()) < 0.01) {
                //金额对比
                //todo 3、保存订单
                saveOrder(order);
                //4、库存锁定。只要有异常回滚订单数据。
                // 订单号，所有订单项（skuId，skuName，skuNum）
                WareSkuLockVo lockVo = new WareSkuLockVo();
                lockVo.setOrderSn(order.getOrder().getOrderSn());
                List<OrderItemVo> locks = order.getOrderItems().stream().map(item -> {
                    OrderItemVo orderItemVo = new OrderItemVo();
                    orderItemVo.setSkuId(item.getSkuId());
                    orderItemVo.setCount(item.getSkuQuantity());
                    orderItemVo.setTitle(item.getSkuName());
                    return orderItemVo;
                }).collect(Collectors.toList());
                lockVo.setLocks(locks);
                // todo 4、远程锁库存
                //库存成功了，但是网络原因超时了，订单回滚，库存不滚
                // 为了保证高并发。库存服务自己回滚。可以发消息给库存服务
                // 库存服务本身也可以使用自动解锁模式 消息队列
                R r = wmsFeignService.orderLockStock(lockVo);
                if (r.getCode() == 0) {
                    // 锁成功
                    response.setOrder(order.getOrder());
                    // todo 5、远程扣减积分 出异常
                    int i = 10 / 0;// 订单回滚，库存不滚
                    return response;
                } else {
                    // 锁定失败
                    String msg = (String) r.get("msg");
//                    response.setCode(3);
                    throw new NoStockException(msg);
                }


            } else {
                response.setCode(2);
                return response;
            }

        }

//        String token = stringRedisTemplate.opsForValue().get(key);
//        if (orderToken != null && orderToken.equals(token)) {
//
//            // 令牌通过
//
//        }else {
//
//
//        }

    }

    @Override
    public OrderEntity getOrderByOrderSn(String orderSn) {

        OrderEntity order_sn = this.getOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));
        return order_sn;
    }

    /**
     * @param order
     * @return void
     * @description 保存订单数据
     * @version V1.0.0
     * @date 12:54 下午 2021/10/4
     * @author tangyao
     */
    private void saveOrder(OrderCreateTo order) {
        OrderEntity orderEntity = order.getOrder();
        orderEntity.setModifyTime(new Date());
        this.save(orderEntity);

        List<OrderItemEntity> orderItems = order.getOrderItems();
        orderItemService.saveBatch(orderItems);


    }

    private OrderCreateTo createOrder() {


        OrderCreateTo orderCreateTo = new OrderCreateTo();
        //1、生成订单号
        String orderSn = IdWorker.getTimeId();
        // 创建订单号
        OrderEntity orderEntity = buildOrder(orderSn);

        //2.获取到所有的订单项
        List<OrderItemEntity> itemEntities = buildOrderItems(orderSn);
        //3、计算价格,积分等相关
        computePrice(orderEntity, itemEntities);
        orderCreateTo.setOrder(orderEntity);
        orderCreateTo.setOrderItems(itemEntities);
        return orderCreateTo;
    }

    private void computePrice(OrderEntity orderEntity, List<OrderItemEntity> itemEntities) {
        BigDecimal total = new BigDecimal("0.0");
        BigDecimal coupon = new BigDecimal("0.0");
        BigDecimal promotion = new BigDecimal("0.0");
        BigDecimal integration = new BigDecimal("0.0");
        Integer giftIntegration = 0;
        Integer giftGrowth = 0;


        // 订单的总额，叠加每一个订单项的总额信息
        for (OrderItemEntity entity : itemEntities) {
            BigDecimal realAmount = entity.getRealAmount();
            coupon = coupon.add(entity.getCouponAmount());
            promotion = promotion.add(entity.getPromotionAmount());
            integration = integration.add(entity.getIntegrationAmount());
            total = total.add(realAmount);
            giftIntegration = giftIntegration + entity.getGiftIntegration();
            giftGrowth = giftGrowth + entity.getGiftGrowth();
        }
        //1、计算订单价格相关
        orderEntity.setTotalAmount(total);
        // 应付总额
        orderEntity.setPayAmount(total.add(orderEntity.getFreightAmount()));
        orderEntity.setPromotionAmount(promotion);
        orderEntity.setIntegrationAmount(integration);
        orderEntity.setCouponAmount(coupon);
        // 设置积分等信息
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        orderEntity.setAutoConfirmDay(7);
        orderEntity.setGrowth(giftGrowth);
        orderEntity.setIntegration(giftIntegration);
        orderEntity.setDeleteStatus(0);// 未删除


    }

    private OrderEntity buildOrder(String orderSn) {

        MemberRespVo memberRespVo = LoginUserInterceptor.loginUser.get();

        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(orderSn);
        orderEntity.setMemberId(memberRespVo.getId());

        OrderSubmitVo orderSubmitVo = confirmVoThreadLocal.get();
        //获取收货地址信息
        R fare = wmsFeignService.getFare(orderSubmitVo.getAddrId());
        FareVo fareResp = fare.getData(new TypeReference<FareVo>() {
        });


        // 设置运费信息
        orderEntity.setFreightAmount(fareResp.getFare());
        // 设置收货人信息
        MemberAddressVo address = fareResp.getAddress();
        orderEntity.setReceiverCity(address.getCity());
        orderEntity.setReceiverDetailAddress(address.getDetailAddress());
        orderEntity.setReceiverName(address.getName());
        orderEntity.setReceiverPhone(address.getPhone());
        orderEntity.setReceiverPostCode(address.getPostCode());
        orderEntity.setReceiverProvince(address.getProvince());
        orderEntity.setReceiverRegion(address.getRegion());
        return orderEntity;
    }

    /**
     * @param
     * @param orderSn
     * @return com.atguigu.gulimall.order.entity.OrderItemEntity
     * @description 构建订单项数据
     * @version V1.0.0
     * @date 2:52 下午 2021/10/3
     * @author tangyao
     */
    private List<OrderItemEntity> buildOrderItems(String orderSn) {
        //最后确定每个购物项的价格
        List<OrderItemVo> currentUserCartItems = cartFeignService.getCurrentUserCartItems();
        if (currentUserCartItems != null && currentUserCartItems.size() > 0) {
            List<OrderItemEntity> itemEntities = currentUserCartItems.stream().map(cartItem -> {
                OrderItemEntity itemEntity = buildOrderItem(cartItem);
                itemEntity.setOrderSn(orderSn);

                return itemEntity;
            }).collect(Collectors.toList());
            return itemEntities;
        }

        return null;
    }

    /**
     * @param cartItem
     * @return com.atguigu.gulimall.order.entity.OrderItemEntity
     * @description 构建每一个订单项
     * @version V1.0.0
     * @date 2:59 下午 2021/10/3
     * @author tangyao
     */
    private OrderItemEntity buildOrderItem(OrderItemVo cartItem) {

        OrderItemEntity itemEntity = new OrderItemEntity();

        //1、订单信息： 订单号
        //2、商品的spu信息
        Long skuId = cartItem.getSkuId();

        R r = productFeignService.getSpuInfoBySkuId(skuId);
        SpuInfoVo data = r.getData(new TypeReference<SpuInfoVo>() {
        });
        itemEntity.setSpuId(data.getId());
        itemEntity.setSpuBrand(data.getBrandId().toString());
        itemEntity.setSkuName(data.getSpuName());
        itemEntity.setCategoryId(data.getCatalogId());


        //3、商品的sku信息
        itemEntity.setSkuId(cartItem.getSkuId());
        itemEntity.setSkuName(cartItem.getTitle());
        itemEntity.setSkuPic(cartItem.getImage());
        itemEntity.setSkuPrice(cartItem.getPrice());
        String skuAttr = StringUtils.collectionToDelimitedString(cartItem.getSkuAttr(), ";");
        itemEntity.setSkuAttrsVals(skuAttr);
        itemEntity.setSkuQuantity(cartItem.getCount());
        //4、优惠信息（不做）
        //5、积分信息
        itemEntity.setGiftGrowth(cartItem.getPrice().multiply(new BigDecimal(cartItem.getCount().toString())).intValue());
        itemEntity.setGiftIntegration(cartItem.getPrice().multiply(new BigDecimal(cartItem.getCount().toString())).intValue());
        //6、订单项的价格信息
        itemEntity.setPromotionAmount(new BigDecimal("0"));
        itemEntity.setCouponAmount(new BigDecimal("0"));
        itemEntity.setIntegrationAmount(new BigDecimal("0"));
        // 当前订单项的实际金额 总额减去各种优惠
        BigDecimal amount = itemEntity.getSkuPrice().multiply(new BigDecimal(itemEntity.getSkuQuantity().toString()));
        BigDecimal realAmount = amount.subtract(itemEntity.getPromotionAmount()).subtract(itemEntity.getCouponAmount()).subtract(itemEntity.getIntegrationAmount());
        itemEntity.setRealAmount(realAmount);


        return itemEntity;
    }

}