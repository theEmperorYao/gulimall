package com.atguigu.gulimall.product.exception;

import com.atguigu.common.exception.BizCodeEnum;
import com.atguigu.common.utils.R;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author tangyao
 * @version 1.0.0
 * @Description TODO
 * @createTime 2020年10月04日 20:15:00
 */
@Slf4j
@RestControllerAdvice(basePackages ="com.atguigu.gulimall.product.controller")
public class GulimallExceptionControllerAdvance2  {

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public R handleValidException(MethodArgumentNotValidException e){
        log.error("异常是：{},类型是：{}",e.getMessage(),e.getClass());
        Map<String,Object> map=new HashMap<>();
        BindingResult bindingResult = e.getBindingResult();
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        fieldErrors.forEach(item->{
            String field = item.getField();
            String defaultMessage = item.getDefaultMessage();
            map.put(field,defaultMessage);
        });


        return R.error(BizCodeEnum.VALID_EXCEPTION.getCode(),BizCodeEnum.VALID_EXCEPTION.getMessage()).put("data",map);
    }

    @ExceptionHandler(value = Throwable.class)
    public R handleException(Throwable throwable){
        log.error("错误：",throwable);
        return R.error(BizCodeEnum.UNKNOW_EXCEPTION.getCode(),BizCodeEnum.UNKNOW_EXCEPTION.getMessage());
    }
}
