package com.sky.annotation;


import com.sky.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author 喜欢悠然独自在
 * @version 1.0
 * 自定义注解,用于标记某个方法需要进行公共字段的自动填充
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoFill {
    //定义枚举类对象,内含数据库操作类型:UPDATE INSERT
    OperationType value();

}
