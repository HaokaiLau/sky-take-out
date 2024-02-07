package com.sky.mapper;

import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;

import java.util.ArrayList;

/**
 * @author 喜欢悠然独自在
 * @version 1.0
 */
@Mapper
public interface OrderDetailMapper {

    /**
     * 批量插入
     * @param orderDetailList
     */
    void insertBatch(ArrayList<OrderDetail> orderDetailList);
}
