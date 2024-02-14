package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
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

    /**
     * 营业额统计
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
            map.put("begin",beginTime);
            map.put("end",endTime);
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
                .dateList(StringUtils.join(dateList,","))//把集合中的每一个数据以字符串的形式用逗号拼接起来
                .turnoverList(StringUtils.join(trunoverList,","))//把集合中的每一个数据以字符串的形式用逗号拼接起来
                .build();
    }
}
