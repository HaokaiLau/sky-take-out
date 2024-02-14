package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 喜欢悠然独自在
 * @version 1.0
 */
@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;

    /**
     * 营业额统计
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        //该集合用于存放begin到end范围内的每天的日期
        List<LocalDate> dateList = new ArrayList<>();
        //集合中第一个数据就是begin
        dateList.add(begin);
        //while判断日期是否到了end 没有就往后加一天,然后添加到集合中
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        List<Double> trunoverList = new ArrayList<>();

        //遍历日期集合
        for (LocalDate date : dateList) {
            //得出该日的0时0分0秒和该日的23时59分59秒
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            //根据该日的起始时间和结束时间以及订单状态(已完成)来查询数据库
            Map map = new HashMap();
            map.put("begin", beginTime);
            map.put("end", endTime);
            map.put("status", Orders.COMPLETED);
            //select sum(amount) from orders where order_time > #{} and order_time < #{} and status = #{status}
            Double turnover = orderMapper.sumByMap(map);
            //如果当天营业额数据为空,那就赋默认值0.0
            turnover = turnover == null ? 0.0 : turnover;
            //把遍历出来的当天营业额添加到集合中
            trunoverList.add(turnover);
        }

        //封装返回结果
        return TurnoverReportVO
                .builder()
                .dateList(StringUtils.join(dateList, ","))//把集合中的每一个数据以字符串的形式用逗号拼接起来
                .turnoverList(StringUtils.join(trunoverList, ","))//把集合中的每一个数据以字符串的形式用逗号拼接起来
                .build();
    }

    /**
     * 用户统计
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        //把计算日期范围内所有日期的操作封装成一个方法,调用方法得到每一日的日期集合
        List<LocalDate> dateList = getDateList(begin, end);

        //查询数据库,得到每一天的用户总量,封装成集合返回
        List<Integer> totalUserList = new ArrayList<>();

        //查询数据库,得到每一天新增的用户,封装成集合返回
        List<Integer> newUserList = new ArrayList<>();

        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map map = new HashMap();

            //先存入截止时间,查询截止到该日的用户总量
            map.put("end", endTime);
            Integer totalUser = userMapper.countByMap(map);
            totalUserList.add(totalUser);

            //再存入起始时间,结合截止时间查询该时间范围内新增的用户数
            map.put("begin", beginTime);
            Integer newUser = userMapper.countByMap(map);
            //如果当天没有新增用户,则赋予默认值0
            newUser = newUser == null ? 0 : newUser;
            newUserList.add(newUser);
        }

        //封装成UserReportVO对象返回
        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .totalUserList(StringUtils.join(totalUserList, ","))
                .build();
    }

    /**
     * 订单统计
     *
     * @return
     */
    @Override
    public OrderReportVO getOrdersStatistics(LocalDate begin, LocalDate end) {

        LocalDateTime deadline = LocalDateTime.of(end, LocalTime.MAX);

        List<LocalDate> dateList = getDateList(begin, end);

        //用于存放每日订单总数
        List<Integer> orderCountList = new ArrayList<>();

        //存放每日有效订单数
        List<Integer> validOrderCountList = new ArrayList<>();

        //遍历dateList
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            //查询每天的订单总数
            Integer orderCount = getOrderCount(beginTime, endTime, null);
            orderCount = orderCount == null ? 0 : orderCount;
            orderCountList.add(orderCount);

            //查询每天的有效订单数
            Integer validOrderCount = getOrderCount(beginTime, endTime, Orders.COMPLETED);
            validOrderCount = validOrderCount == null ? 0 : validOrderCount;
            validOrderCountList.add(validOrderCount);

        }

        //查询总订单数
        Integer totalOrderCount = 0;
        for (Integer orderCount : orderCountList) {
            totalOrderCount += orderCount;
        }

        //查询总有效订单数
        Integer validOrderCount = 0;
        for (Integer orderCount : validOrderCountList) {
            validOrderCount += orderCount;
        }

        //计算订单完成率
        Double orderCompletionRate = 0.0;
        //判断分母是否为0
        if (totalOrderCount != 0) {
            orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;
        }

        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    /**
     * 计算指定范围内所有日期的方法
     *
     * @param begin
     * @param end
     * @return
     */
    private List<LocalDate> getDateList(LocalDate begin, LocalDate end) {
        //该集合用于存放begin到end范围内的每天的日期
        List<LocalDate> dateList = new ArrayList<>();
        //集合中第一个数据就是begin
        dateList.add(begin);
        //while判断日期是否到了end 没有就往后加一天,然后添加到集合中
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        return dateList;
    }

    /**
     * 计算订单数量的方法
     *
     * @param begin
     * @param end
     * @param status
     * @return
     */
    private Integer getOrderCount(LocalDateTime begin, LocalDateTime end, Integer status) {
        Map map = new HashMap<>();
        map.put("begin", begin);
        map.put("end", end);
        map.put("status", status);
        return orderMapper.countByMap(map);
    }

}
