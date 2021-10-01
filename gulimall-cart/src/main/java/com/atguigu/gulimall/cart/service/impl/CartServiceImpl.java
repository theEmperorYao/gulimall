package com.atguigu.gulimall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.cart.config.ThreadPoolConfigProperties;
import com.atguigu.gulimall.cart.feign.ProductFeignService;
import com.atguigu.gulimall.cart.interceptor.CartInterceptor;
import com.atguigu.gulimall.cart.service.CartService;
import com.atguigu.gulimall.cart.vo.Cart;
import com.atguigu.gulimall.cart.vo.CartItem;
import com.atguigu.gulimall.cart.vo.SkuInfoVo;
import com.atguigu.gulimall.cart.vo.UserInfoTo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @Classname CartServiceImpl
 * @Description TODO
 * @Date 2021/9/26 5:35 下午
 * @Created by tangyao
 */
@Slf4j
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    private ThreadPoolExecutor executor;


    private final String CART_PREFIX = "gulimall:cart:";

    @Override
    public CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException {
        BoundHashOperations cartOps = getCartOps();


        String o = (String) cartOps.get(skuId.toString());
        // 购物车由此商品，修改数量
        if (!StringUtils.isEmpty(o)) {
            CartItem cartItem = JSON.parseObject(o, CartItem.class);
            cartItem.setCount(cartItem.getCount() + num);
            String s = JSON.toJSONString(cartItem);
            cartOps.put(skuId.toString(), s);
            return cartItem;
        }

        // 购物车无此商品
        CartItem cartItem = new CartItem();
        //添加新商品到购物车

        //1、远程查询当前要添加的商品信息
        CompletableFuture<Void> getSkuInfoTask = CompletableFuture.runAsync(() -> {
            R skuInfo = productFeignService.getSkuInfo(skuId);
            SkuInfoVo data = skuInfo.getData("skuInfo", new TypeReference<SkuInfoVo>() {
            });

            cartItem.setCheck(true);
            cartItem.setCount(num);
            cartItem.setImage(data.getSkuDefaultImg());
            cartItem.setPrice(data.getPrice());
            cartItem.setTitle(data.getSkuTitle());
            cartItem.setSkuId(data.getSkuId());
            cartItem.setPrice(data.getPrice());
        }, executor);


        //2、远程查询sku的组合信息
        CompletableFuture<Void> getSkuSaleAttrValues = CompletableFuture.runAsync(() -> {
            List<String> skuSaleAttrValues = productFeignService.getSkuSaleAttrValues(skuId);
            cartItem.setSkuAttr(skuSaleAttrValues);
        }, executor);

        CompletableFuture.allOf(getSkuInfoTask, getSkuSaleAttrValues).get();

        // 如果直接放，会对象转化为字符流（默认是jdk序列化），存在redis，为了能识别转化为JSON串
        String s = JSON.toJSONString(cartItem);
        cartOps.put(skuId.toString(), s);

        return cartItem;
    }

    @Override
    public CartItem getCartItem(Long skuId) {

        BoundHashOperations<String, Object, Object> cartOps = getCartOps();

        String o = (String) cartOps.get(skuId.toString());

        CartItem cartItem = JSON.parseObject(o, CartItem.class);

        return cartItem;
    }

    @Override
    public Cart getCart() throws ExecutionException, InterruptedException {
        // 1、登录
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();

        Cart cart = new Cart();

        if (userInfoTo.getUserId() != null) {
            //1、登录
            String cartKey = CART_PREFIX + userInfoTo.getUserId();
//            BoundHashOperations<String, Object, Object> hashOps = stringRedisTemplate.boundHashOps(cartKey);
            //2、如果临时购物车里面的数据还没有合并[合并购物车]，
            String tempCartKey = CART_PREFIX + userInfoTo.getUserKey();
            List<CartItem> tempCartItem = getCartItems(tempCartKey);
            if (tempCartItem != null) {
                for (CartItem cartItem : tempCartItem) {
                    addToCart(cartItem.getSkuId(), cartItem.getCount());
                }
                //清除临时购物车的数据
                clearCart(tempCartKey);
            }
            //3、获取登录后的购物车的数据【包含合并过来的临时购物车的数据，和登录后的购物车的数据】
            List<CartItem> cartItems = getCartItems(cartKey);
            cart.setItems(cartItems);

        } else {
            //2、没登录
            String cartKey = CART_PREFIX + userInfoTo.getUserKey();
            // 获取临时购物车的所有购物项
            List<CartItem> cartItems = getCartItems(cartKey);
            cart.setItems(cartItems);

        }

        return cart;
    }

    /**
     * @return org.springframework.data.redis.core.BoundHashOperations
     * @description 获取到我们要操作的购物车
     * @version V1.0.0
     * @date 4:41 下午 2021/9/27
     * @author tangyao
     */
    private BoundHashOperations<String, Object, Object> getCartOps() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        //1、确认redis的key
        String cartKey = "";
        if (userInfoTo.getUserId() != null) {
            cartKey = CART_PREFIX + userInfoTo.getUserId();
        } else {
            cartKey = CART_PREFIX + userInfoTo.getUserKey();
        }

//        stringRedisTemplate.opsForHash().get(cartKey,"1");
        BoundHashOperations<String, Object, Object> operations = stringRedisTemplate.boundHashOps(cartKey);
        return operations;
    }

    private List<CartItem> getCartItems(String cartKey) {
        BoundHashOperations<String, Object, Object> hashOps = stringRedisTemplate.boundHashOps(cartKey);
        List<Object> values = hashOps.values();
        if (values != null && values.size() > 0) {
            List<CartItem> collect = values.stream().map(o -> {
                String str = (String) o;
                CartItem cartItem = JSON.parseObject(str, CartItem.class);
                return cartItem;
            }).collect(Collectors.toList());
            return collect;
        }
        return null;
    }

    @Override
    public void clearCart(String cartKey) {
        stringRedisTemplate.delete(cartKey);
    }

    @Override
    public void checkItem(Long skuId, Integer check) {
        CartItem cartItem = getCartItem(skuId);
        cartItem.setCheck(check == 1);
        String s = JSON.toJSONString(cartItem);
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.put(skuId.toString(), s);
    }

    @Override
    public void changeItemCount(Long skuId, Integer num) {
        CartItem cartItem = getCartItem(skuId);
        cartItem.setCount(num);
        String s = JSON.toJSONString(cartItem);
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.put(skuId.toString(), s);
    }

    @Override
    public void deleteItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.delete(skuId.toString());
    }

    @Override
    public List<CartItem> getUserCartItems() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if (userInfoTo.getUserId() == null) {
            return null;
        }
        String cartKey = CART_PREFIX + userInfoTo.getUserId();
        List<CartItem> collect = getCartItems(cartKey).stream()
                .filter(item -> item.getCheck())
                .map(item -> {
                    // 更新为最新价格
                    R price = productFeignService.getPrice(item.getSkuId());
                    String data = (String) price.get("data");
                    item.setPrice(new BigDecimal(data));
                    return item;
                }).collect(Collectors.toList());
        return collect;

    }
}
