package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.Setmeal;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * @author 喜欢悠然独自在
 * @version 1.0
 */
@Mapper
public interface SetmealMapper {
    /**
     * 根据category_id查询数量
     * @param id
     * @return
     */
    @Select("select count(*) from setmeal where category_id = #{id}")
    Integer countByCategoryId(Long id);

    /**
     * 插入套餐
     */
    @AutoFill(value = OperationType.INSERT)
    void insert(Setmeal setmeal);
}
