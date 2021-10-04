package com.atguigu.gulimall.cart.controller;

import com.atguigu.gulimall.cart.service.CartService;
import com.atguigu.gulimall.cart.vo.Cart;
import com.atguigu.gulimall.cart.vo.CartItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @Classname CartController
 * @Description TODO
 * @Date 2021/9/26 5:41 下午
 * @Created by tangyao
 */
@Controller
public class CartController {


    @Autowired
    private CartService cartService;

    @ResponseBody
    @GetMapping("/currentUserCartItems")
    public List<CartItem> getCurrentUserCartItems(){

        return cartService.getUserCartItems();
    }



    @GetMapping("/deleteItem")
    public String deleteItem(@RequestParam("skuId") Long skuId) {
        cartService.deleteItem(skuId);
        return "redirect:http://cart.gulimall.com/cart.html";
    }

    @GetMapping("/countItem")
    public String countItem(@RequestParam("skuId") Long skuId,
                            @RequestParam("num") Integer num) {
        cartService.changeItemCount(skuId, num);
        return "redirect:http://cart.gulimall.com/cart.html";
    }


    @GetMapping("/checkItem.html")
    public String checkItem(@RequestParam("skuId") Long skuId,
                            @RequestParam("check") Integer check) {
        cartService.checkItem(skuId, check);
        return "redirect:http://cart.gulimall.com/cart.html";
    }


    /**
     * @return java.lang.String
     * @description 浏览器有一个cookie，user-key：标识用户身份，一个月之后过期
     * 如果第一次使用jd的购物车功能，都会给一个临时的用户身份
     * 浏览器保存以后，每次访问都会带上这个cookie
     * <p>
     * 登录：session有
     * 没登录：按照cookie里面带来的user-key来做。
     * 第一次：如果没有临时用户，帮忙创建一个临时用户
     * @version V1.0.0
     * @date 6:57 下午 2021/9/26
     * @author tangyao
     */
    @GetMapping("cart.html")
    public String cartListPage(Model model) throws ExecutionException, InterruptedException {

        //1、快速得到用户信息，id，user-key

        Cart cart = cartService.getCart();
        model.addAttribute("cart", cart);
        return "cartList";
    }

    /**
     * @return java.lang.String
     * @description redirectAttributes.addFlashAttribute()
     * 将数据放在session里面可以在页面取出，但是只能取一次
     * redirectAttributes.addAttribute("skuId", skuId);
     * 将数据放在url后面
     * @version V1.0.0
     * @date 10:40 下午 2021/9/26
     * @author tangyao
     */
    @GetMapping("/addToCart")
    public String addToCart(@RequestParam("skuId") Long skuId,
                            @RequestParam("num") Integer num,
                            RedirectAttributes redirectAttributes) throws ExecutionException, InterruptedException {

        cartService.addToCart(skuId, num);
        redirectAttributes.addAttribute("skuId", skuId);
        return "redirect:http://cart.gulimall.com/addToCartSuccess.html";
    }


    /**
     * @param skuId
     * @param model
     * @return java.lang.String
     * @description 跳转到成功页
     * @version V1.0.0
     * @date 12:25 上午 2021/9/28
     * @author tangyao
     */
    @GetMapping("addToCartSuccess.html")
    public String addToCartSuccessPage(@RequestParam("skuId") Long skuId,
                                       Model model) {
        // 重定向到成功页面。再次查询购物车数据即可
        CartItem cartItem = cartService.getCartItem(skuId);
        ;
        model.addAttribute("item", cartItem);


        return "success";
    }

}















