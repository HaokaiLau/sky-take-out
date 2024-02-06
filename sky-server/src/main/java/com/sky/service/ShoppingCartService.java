package com.sky.service;

import com.sky.dto.ShoppingCartDTO;

/**
 * @author 喜欢悠然独自在
 * @version 1.0
 */
public interface ShoppingCartService {

    /**
     * 添加购物车
     * @param shoppingCartDTO
     */
    void addShoppingCart(ShoppingCartDTO shoppingCartDTO);
}
