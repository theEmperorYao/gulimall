package com.atguigu.gulimall.order.web;

import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.vo.OrderConfirmVo;
import com.atguigu.gulimall.order.vo.OrderSubmitVo;
import com.atguigu.gulimall.order.vo.SubmitOrderResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.concurrent.ExecutionException;

/**
 * @Classname OrderWebController
 * @Description TODO
 * @Date 2021/10/1 4:18 下午
 * @Created by tangyao
 */
@Controller
public class OrderWebController {


    @Autowired
    OrderService orderService;

    @GetMapping("/toTrade")
    public String toTrade(Model model) throws ExecutionException, InterruptedException {
        OrderConfirmVo confirmVo = orderService.confirmOrder();
        model.addAttribute("orderConfirmData", confirmVo);
        return "confirm";
    }

    /**
     * @param orderSubmitVo
     * @return java.lang.String
     * @description 下单功能
     * @version V1.0.0
     * @date 10:29 下午 2021/10/2
     * @author tangyao
     */
    @PostMapping("/submitOrder")
    public String submitOrder(OrderSubmitVo orderSubmitVo, Model model, RedirectAttributes redirectAttributes) {


        SubmitOrderResponseVo responseVo = orderService.submitOrder(orderSubmitVo);

        if (responseVo.getCode() == 0) {
            // 成功
            // 去创建订单，验令牌，验价格，锁库存
            model.addAttribute("submitOrderResp", responseVo);
            return "pay";

        } else {

            String msg = "下单失败";
            switch (responseVo.getCode()) {
                case 1: msg+="订单信息过期，请刷新再次提交";break;
                case 2: msg+="订单商品价格发生变化，请确认后再次提交";break;
                case 3: msg+="库存锁定失败，商品库存不足";break;
            }
            redirectAttributes.addFlashAttribute("msg", msg);
            return "redirect:http://order.gulimall.com/toTrade";
        }

        // 下单成功来到支付选择页
        // 下单失败回到订单确认页
//        System.out.println("订单提交的数据。。。" + orderSubmitVo);


    }

}
