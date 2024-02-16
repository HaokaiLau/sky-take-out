package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
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
    @Autowired
    private WorkspaceService workspaceService;

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
     * 销量top10
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public SalesTop10ReportVO getTop10(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        List<GoodsSalesDTO> salesTop10 = orderMapper.getSalesTop10(beginTime, endTime);
        //用于存放商品名称
        List<String> nameList = new ArrayList<>();
        //用于存放商品数量
        List<Integer> numberList = new ArrayList<>();
        for (GoodsSalesDTO goodsSalesDTO : salesTop10) {
            nameList.add(goodsSalesDTO.getName());
            numberList.add(goodsSalesDTO.getNumber());
        }

        //封装成VO对象返回
        return SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(nameList, ","))
                .numberList(StringUtils.join(numberList, ","))
                .build();
    }

    /**
     * 导出运营数据报表
     *
     * @param response
     */
    @Override
    public void exportBusinessData(HttpServletResponse response) {
        //查询数据库获得数据
        LocalDate dateBegin = LocalDate.now().plusDays(-30);
        LocalDate dateEnd = LocalDate.now().minusDays(1);
        LocalDateTime begin = LocalDateTime.of(dateBegin, LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(dateEnd, LocalTime.MAX);

        BusinessDataVO businessDataVO = workspaceService.getBusinessData(begin, end);

        //通过POI把数据写入到Excel文件中
        try {
            //通过反射获取类路径下的模板文件输入流
            InputStream ips = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");

            //基于模板文件创建一个新的Excel文件
            XSSFWorkbook excel = new XSSFWorkbook(ips);

            //获取表格文件中的Sheet页
            XSSFSheet sheet = excel.getSheet("Sheet1");

            //填充数据----时间 行列都是从0开始
            sheet.getRow(1).getCell(1).setCellValue("时间：" + dateBegin + "至" + dateEnd);

            //填充第4行的数据
            XSSFRow row = sheet.getRow(3);
            row.getCell(2).setCellValue(businessDataVO.getTurnover());
            row.getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessDataVO.getNewUsers());

            //填充第5行的数据
            row = sheet.getRow(4);
            row.getCell(2).setCellValue(businessDataVO.getValidOrderCount());
            row.getCell(4).setCellValue(businessDataVO.getUnitPrice());

            //使用循环补充剩余的表格数据
            for (int i = 0; i < 30; i++) {
                //计算每日日期
                LocalDate date = dateBegin.plusDays(i);
                //查询数据库该日的数据
                BusinessDataVO businessData = workspaceService.getBusinessData(
                        LocalDateTime.of(date, LocalTime.MIN),
                        LocalDateTime.of(date, LocalTime.MAX));
                //得到某一行
                row = sheet.getRow(7 + i);
                //为该行的每一列填充数据
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(businessData.getTurnover());
                row.getCell(3).setCellValue(businessData.getValidOrderCount());
                row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
                row.getCell(5).setCellValue(businessData.getUnitPrice());
                row.getCell(6).setCellValue(businessData.getNewUsers());
            }

            //通过输出流把Excel文件下载到客户端浏览器中
            ServletOutputStream ops = response.getOutputStream();
            excel.write(ops);

            //关闭所有打开的资源
            ips.close();
            excel.close();
            ops.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

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
