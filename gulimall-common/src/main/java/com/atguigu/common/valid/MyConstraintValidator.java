package com.atguigu.common.valid;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

/**
 * @author tangyao
 * @version 1.0.0
 * @Description TODO
 * @createTime 2020年10月05日 08:25:00
 */
public class MyConstraintValidator implements ConstraintValidator<OnlyOneOrEero,Integer> {
    Set<Integer> set=new HashSet<>();

    @Override
    public void initialize(OnlyOneOrEero constraintAnnotation) {
        int[] vals = OnlyOneOrEero.vals;
        for (int val : vals) {
            set.add(val);
        }
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {

        return set.contains(value);
    }
}
