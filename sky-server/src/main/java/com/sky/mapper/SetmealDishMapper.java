package com.sky.mapper;

import com.sky.entity.SetmealDish;
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

    /**
     * 插入套餐中包含的菜品集合
     * 操作此表的插入更新操作时无需自动填充公共字段
     * @param setmealDishes
     */
    void insertBatch(List<SetmealDish> setmealDishes);

}
