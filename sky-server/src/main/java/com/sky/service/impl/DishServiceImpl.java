package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author 喜欢悠然独自在
 * @version 1.0
 */
@Service
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;


    /**
     * 新增菜品
     *
     * @param dishDTO
     */
    @Transactional
    @Override
    public void saveWithFlavor(DishDTO dishDTO) {
        //向菜品表插入一条数据
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dish.setStatus(StatusConstant.DISABLE);
        dishMapper.insert(dish);
        //获取插入完成后返回回来的dishId
        Long dishId = dish.getId();

        //向口味表插入n条数据
        //判断有没有传来口味的数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0) {
            flavors.stream().forEach(dishFlavor -> {
                dishFlavor.setDishId(dishId);
            });
            dishFlavorMapper.insertBatch(flavors);
        }


    }

    /**
     * 菜品分页查询
     *
     * @return
     */
    @Override
    public PageResult page(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        Page<DishVO> p = dishMapper.page(dishPageQueryDTO);

        long total = p.getTotal();
        List<DishVO> records = p.getResult();
        PageResult pageResult = new PageResult(total, records);
        return pageResult;
    }

    /**
     * 菜品起售停售
     *
     * @param status
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        Dish dish = Dish.builder()
                .status(status)
                .id(id)
                .build();
        dishMapper.update(dish);
    }

    /**
     * 根据id查询菜品
     * @param id
     * @return
     */
    @Override
    public DishVO getByIdWithFlavor(Long id) {
        //根据菜品id查询菜品
        Dish dish = dishMapper.selectById(id);
        //根据菜品id查询菜品的口味数据
        List<DishFlavor> flavors = dishFlavorMapper.selectById(id);

        //把菜品数据和口味数据封装成一个DishVO对象
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish,dishVO);
        dishVO.setFlavors(flavors);
        return dishVO;
    }

    /**
     * 修改菜品
     * @param dishDTO
     */
    @Transactional
    @Override
    public void updateWithFlavor(DishDTO dishDTO) {
        //把dishDTO对象拆分成dish 和 dishFlavor集合
        //然后分别更新两个表的数据
        Dish dish = new Dish();
        //通过值拷贝获得dish对象
        BeanUtils.copyProperties(dishDTO,dish);
        dishMapper.update(dish);

        //获得dishDTO对象中的菜品口味集合
        Long dishId = dish.getId();
        List<DishFlavor> flavors = dishDTO.getFlavors();
        //把更新操作替换为 删除 + 插入
        dishFlavorMapper.deleteByDishId(dishId);
        //判断菜品口味集合是否为空,不为空才遍历菜品口味集合为菜品口味对象的dishId赋值
        if (flavors != null && flavors.size() > 0) {
            flavors.stream().forEach(dishFlavor -> {
                dishFlavor.setDishId(dishId);
            });
            //把新的菜品口味集合插入到表中
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    /**
     * 批量删除菜品
     * @param ids
     */
    @Transactional
    @Override
    public void deleteBatch(List<Long> ids) {
        for (Long id : ids) {
            //判断菜品是否为起售中的菜品
            if (dishMapper.selectById(id).getStatus() == StatusConstant.ENABLE) {
                //菜品起售中,不能删除
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }

        //判断菜品是否关联着套餐
        List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishIds(ids);
        if (setmealIds != null && setmealIds.size() > 0) {
            //菜品关联着套餐,不能删除
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

        //删除菜品数据和菜品口味数据
        for (Long id : ids) {
            //删除菜品
            dishMapper.deleteById(id);
            //删除菜品相关口味
            dishFlavorMapper.deleteByDishId(id);
        }

    }
}
