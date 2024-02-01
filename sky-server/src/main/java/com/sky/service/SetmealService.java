package com.sky.service;

import com.sky.dto.SetmealDTO;

/**
 * @author 喜欢悠然独自在
 * @version 1.0
 */
public interface SetmealService {

    /**
     * 新增菜品
     * @param setmealDTO
     */
    void saveWithDish(SetmealDTO setmealDTO);
}
