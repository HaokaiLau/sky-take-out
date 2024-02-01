package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author 喜欢悠然独自在
 * @version 1.0
 */
@Mapper
public interface SetmealDishMapper {


    /**
     * 通过菜品id查询关联的套餐id
     * @return
     */
    List<Long> getSetmealIdsByDishIds(List<Long> dishIds);

}
