package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author 喜欢悠然独自在
 * @version 1.0
 * 自定义定时任务类,定时处理订单状态
 */
@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 处理超时订单 每分钟触发一次
     */
    @Scheduled(cron = "0 * * * * ?")//每分钟触发一次
    public void processTimeoutOrder() {
        log.info("定时处理超时订单:{}", LocalDateTime.now());
        //select * from orders where status = ? and order_time &lt; (当前时间 - 15分钟)
        //当前时间减去15分钟
        LocalDateTime time = LocalDateTime.now().plusMinutes(-15);
        //查出所有超时未付款订单
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTimeLT(Orders.PENDING_PAYMENT, time);
        //非空检验
        if (!CollectionUtils.isEmpty(ordersList)) {
            //遍历集合
            for (Orders orders : ordersList) {
                //修改订单状态,更新取消原因和取消时间
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelReason("订单超时,自动取消");
                orders.setCancelTime(LocalDateTime.now());

                orderMapper.update(orders);
            }
        }
    }

    /**
     * 处理一直处于派送中的订单 每天凌晨一点触发一次
     */
    @Scheduled(cron = "0 0 1 * * ?")//每天凌晨一点触发一次
    public void processDeliveryOrder() {
        log.info("定时处理处于派送中的订单:{}", LocalDateTime.now());
        //当前时间是凌晨一点,当前时间减一个小时就是前一天
        LocalDateTime time = LocalDateTime.now().plusHours(-1);
        //非空检验
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTimeLT(Orders.DELIVERY_IN_PROGRESS, time);
        if (!CollectionUtils.isEmpty(ordersList)) {
            for (Orders orders : ordersList) {
                orders.setStatus(Orders.COMPLETED);
                orderMapper.update(orders);
            }
        }
    }

}
