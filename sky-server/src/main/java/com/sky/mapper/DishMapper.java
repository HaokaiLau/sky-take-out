package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * @author 喜欢悠然独自在
 * @version 1.0
 */
@Mapper
public interface DishMapper {
    /**
     * 根据category_id查询数量
     * @param id
     * @return
     */
    @Select("select count(*) from dish where category_id = #{id}")
    Integer countByCategoryId(Long id);
}
