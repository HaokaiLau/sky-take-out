package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * @author 喜欢悠然独自在
 * @version 1.0
 */
@Aspect//切面类
@Component
@Slf4j
public class AutoFillAspect {

    /**
     * 前置通知,拦截到方法后在方法执行前在通知中为公共字段赋值
     *
     * @param joinPoint 连接点
     */
    //切入点表达式1 拦截mapper包下所有的类以及所有的方法
    //切入点表达式2 拦截有@AutoFill注解的方法
    @Before("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFill(JoinPoint joinPoint) {
        log.info("开始进行公共字段的自动填充...");

        //通过连接点获取拦截到的方法的签名对象
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        //通过签名对象获取到方法然后再获取方法上的注解对象
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);
        //通过注解对象获取value里的值
        OperationType operationType = autoFill.value();

        //通过连接点获取当前拦截方法的参数对象数组
        Object[] args = joinPoint.getArgs();
        //作防止空指针判断
        if (args == null || args.length == 0) {
            return;
        }
        //取出里面的参数
        Object entity = args[0];

        //准备要自动填充的数据
        LocalDateTime now = LocalDateTime.now();//当前时间 用于填充更新时间和创建时间
        Long id = BaseContext.getCurrentId();//操作人的id 用于填充更新人id和创建人id

        //根据注解中对应的不同类型为对应的属性赋值 通过反射来获得
        if (operationType == OperationType.INSERT) {
            try {
                Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                //通过反射为对象属性赋值
                setCreateTime.invoke(entity, now);
                setCreateUser.invoke(entity, id);
                setUpdateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, id);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        if (operationType == OperationType.UPDATE) {
            try {
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                setUpdateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, id);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
