package com.atguigu.common.valid;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * @author tangyao
 * @version 1.0.0
 * @Description TODO
 * @createTime 2020年08月27日 19:16:00
 */
public class ListValConstraintValidator implements ConstraintValidator<ListValue,Integer> {
    private Set<Integer> set=new HashSet<>();
    @Override
    public void initialize(ListValue constraintAnnotation) {
        int[] vals = ListValue.vals;
        for (int val : vals) {
            set.add(val);
        }
    }

    /**
     *
     * @param value 需要校验的值
     * @param context
     * @return
     */
    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        return set.contains(value);
    }
}
