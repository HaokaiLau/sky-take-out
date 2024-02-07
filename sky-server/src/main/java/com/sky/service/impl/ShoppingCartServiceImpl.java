package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author 喜欢悠然独自在
 * @version 1.0
 */
@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    /**
     * 添加购物车
     * @param shoppingCartDTO
     */
    @Override
    public void addShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        //判断当前加入的商品购物车内是否存在了
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO,shoppingCart);//ShoppingCartDTO中缺少用户id属性
        shoppingCart.setUserId(BaseContext.getCurrentId());//获取当前登录用户的id并赋值
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);

        //如果已经存在了,则该商品的数量+1
        if (list != null && list.size() > 0) {
            ShoppingCart sc = list.get(0);//一个用户id只能查出一个购物车数据,所以直接获取集合中第一条数据即可
            sc.setNumber(sc.getNumber() + 1);
            //更新数据
            shoppingCartMapper.updateById(sc);
        }else {
            //如果不存在,则需要插入一条购物车数据

            //判断本次添加到购物车中的是菜品还是套餐
            Long dishId = shoppingCartDTO.getDishId();
            if (dishId != null) {//本次添加的是菜品
                Dish dish = dishMapper.selectById(dishId);
                shoppingCart.setName(dish.getName());
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setAmount(dish.getPrice());
            }else {//本次添加的是套餐
                Long setmealId = shoppingCartDTO.getSetmealId();
                Setmeal setmeal = setmealMapper.selectById(setmealId);
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setImage(setmeal.getImage());
                shoppingCart.setAmount(setmeal.getPrice());
            }
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.insert(shoppingCart);

        }

    }

    /**
     * 根据用户id查询购物车数据
     * @return
     */
    @Override
    public List<ShoppingCart> showShoppingCart() {
        ShoppingCart shoppingCart = ShoppingCart.builder()
                .userId(BaseContext.getCurrentId())//获取用户id为购物车对象赋值
                .build();
        return shoppingCartMapper.list(shoppingCart);
    }

    /**
     * 清空购物车
     */
    @Override
    public void clean() {
        shoppingCartMapper.deleteAll(BaseContext.getCurrentId());
    }

    /**
     * 删除购物车中一个商品
     * @param shoppingCartDTO
     */
    @Override
    public void subShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        //先查询当前用户id的购物车数据
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO,shoppingCart);
        shoppingCart.setUserId(BaseContext.getCurrentId());
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);

        //判断取出来是否有数据
        if (list != null && list.size() > 0) {
            ShoppingCart sc = list.get(0);//因为一个用户只能查出来一个购物车数据,所以直接取第一个数据就行

            Integer number = sc.getNumber();//查看当前商品的数量
            if (number == 1) {
                //如果商品数量等于1,则直接删除商品
                shoppingCartMapper.deleteById(sc.getId());
            } else {
                //如果商品数量大于1,则修改商品数量即可
                sc.setNumber(sc.getNumber() - 1);
                shoppingCartMapper.updateById(sc);

            }
        }
    }
}
