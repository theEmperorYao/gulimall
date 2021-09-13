package com.atguigu.gulimall.auth.controller;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.common.exception.BizCodeEnum;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.auth.feign.MemberFeignService;
import com.atguigu.gulimall.auth.feign.ThirdPartyFeignService;
import com.atguigu.gulimall.auth.vo.UserRegisterVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <p>Title: LoginController</p>
 * Description：登录注册模块
 * date：2020/6/25 13:02
 */
@Slf4j
@Controller
public class LoginController {


    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private ThirdPartyFeignService thirdPartyFeignService;

    @Autowired
    private MemberFeignService memberFeignService;

//	@GetMapping({"/login.html","/","/index","/index.html"})
//	public String loginPage(HttpSession session){
//		Object attribute = session.getAttribute(AuthServerConstant.LOGIN_USER);
//		if(attribute == null){
//			return "login";
//		}
//		return "redirect:http://gulimall.com";
//	}


    @ResponseBody
    @GetMapping("/sms/sendcode")
    public R sendCode(@RequestParam("phone") String phone) {

        //todo1 接口防刷
        String redisCode = stringRedisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);

        if (!Strings.isEmpty(redisCode)) {
            long time = Long.parseLong(redisCode.split("_")[1]);
            if (System.currentTimeMillis() - time < 60000) {
                BizCodeEnum smsCodeException = BizCodeEnum.SMS_CODE_EXCEPTION;
                return R.error(smsCodeException.getCode(), smsCodeException.getMessage());
            }
        }
        // 缓存验证码
        redisCode = UUID.randomUUID().toString().substring(0, 6);
        String redis_code = redisCode + "_" + System.currentTimeMillis();
        stringRedisTemplate.opsForValue()
                .set(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone, redis_code, 10, TimeUnit.MINUTES);
        try {
            return thirdPartyFeignService.sendCode(phone, redisCode);
        } catch (Exception e) {
            log.warn("远程调用不知名错误 [无需解决]");
        }
        return R.ok();

    }


    /**
     * // todo 重定向携带数据，利用session原理。将数据放在session中。
     * 只要跳到下一个页面取出这个数据后，session里面的数据就会被删掉
     * // todo 1.分布式下的session问题。
     *
     * @return java.lang.String
     * @description 注册要提交表单数据 所以用post请求
     * @version V1.0.0
     * @date 7:32 下午 2021/9/10
     * @author tangyao
     */
    @PostMapping("/register")
    public String register(@Valid UserRegisterVo userRegisterVo,
                           BindingResult result,
//                           Model model
//                         redirectAttributes 重定向视图还想携带数据
                           RedirectAttributes redirectAttributes,
                           HttpSession session) {

        if (result.hasErrors()) {
            Map<String, String> errors = result.getFieldErrors().stream()
                    .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));

            // Request method 'POST' not supported
            // 用户注册 --> /register[post] --> 转发 /reg.html（路径映射默认都是get方式访问的）
            // reg.html 以前做 的是路径映射 registry.addViewController("/reg.html").setViewName("reg");

//            model.addAttribute("errors", errors);
            redirectAttributes.addFlashAttribute("errors", errors);

            // 校验出错 ，转发到注册页
//            return "forward:/reg.html";
            // 直接渲染
            // https://blog.csdn.net/liubin5620/article/details/79922692
            // 重定向只能重定向到路径地址，不能到模板引擎 ，
//            return "redirect:/reg.html";
//            session.setAttribute();
            // 重定向要改成域名http://192.168.199.126:20000/reg.html
            return "redirect:http://auth.gulimall.com/reg.html";
        }


        //1、检验验证码

        String code = userRegisterVo.getCode();
        String key = AuthServerConstant.SMS_CODE_CACHE_PREFIX + userRegisterVo.getPhone();
        String s = stringRedisTemplate.opsForValue().get(key);
        if (StringUtils.isNotEmpty(s) && code.equals(s.split("_")[0])) {

            // 删除验证码 令牌机制
            stringRedisTemplate.delete(key);
            // 验证码通过        // 真正注册，调用远程服务进行注册
            R r = memberFeignService.register(userRegisterVo);
            if (r.getCode() == 0) {
                // 登录页和注册页必须加域名，否则不能过nginx 不能访问到静态资源
                return "redirect:http://auth.gulimall.com/login.html";
            } else {
                Map<String, String> errors = new HashMap<>();
                errors.put("msg", r.getData(new TypeReference<String>() {
                }));
                redirectAttributes.addFlashAttribute("errors", errors);
                return "redirect:http://auth.gulimall.com/reg.html";
            }

        } else {
            Map<String, String> errors = new HashMap<>();
            errors.put("code", "验证码错误");
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.gulimall.com/reg.html";
        }

        // 注册成功回到首页，回到登录页
        // '/' 代表以项目域名路径为准 重定向方式数据重复提交
//        return "redirect:/login.html";
    }


}





















