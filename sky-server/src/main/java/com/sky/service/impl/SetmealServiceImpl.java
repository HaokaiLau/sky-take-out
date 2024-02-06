package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
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
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Autowired
    private DishMapper dishMapper;

    /**
     * 新增菜品
     *
     * @param setmealDTO
     */
    @Transactional
    @Override
    public void saveWithDish(SetmealDTO setmealDTO) {
        //把一个套餐插入到套餐表中
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.insert(setmeal);
        //获取从插入操作返回来的setmealId
        Long setmealId = setmeal.getId();

        //把n个套餐菜品插入到套餐菜品表中
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if (setmealDishes != null && setmealDishes.size() > 0) {
            //为套餐中每一个菜品的setmealId赋值
            setmealDishes.stream().forEach(setmealDish -> {
                setmealDish.setSetmealId(setmealId);
            });
        }
        //在套餐菜品表中使用foreach批量插入数据
        setmealDishMapper.insertBatch(setmealDishes);
    }

    /**
     * 套餐分页查询
     *
     * @param setmealPageQueryDTO
     * @return
     */
    @Override
    public PageResult page(SetmealPageQueryDTO setmealPageQueryDTO) {
        //设置分页参数
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> p = setmealMapper.page(setmealPageQueryDTO);
        //返回一个封装好的PageResult对象
        long total = p.getTotal();
        List<SetmealVO> records = p.getResult();
        return new PageResult(total, records);
    }

    /**
     * 套餐起售停售
     *
     * @param status
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        //起售套餐时,判断套餐内是否有停售的菜品,有则无法起售
        if (status == StatusConstant.ENABLE) {
            List<Dish> dishList = dishMapper.getDishIdBySetmealId(id);
            if (dishList != null && dishList.size() > 0) {
                dishList.stream().forEach(dish -> {
                    if (dish.getStatus() == StatusConstant.DISABLE) {
                        throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                    }
                });
            }
        }

        Setmeal setmeal = Setmeal.builder()
                .id(id)
                .status(status)
                .build();
        setmealMapper.update(setmeal);
    }

    /**
     * 根据id查询套餐
     *
     * @return
     */
    @Override
    public SetmealVO getById(Long id) {
        //查询套餐表获取套餐
        Setmeal setmeal = setmealMapper.selectById(id);

        //查询setmeal_dish获取套餐内包含的菜品集合
        List<SetmealDish> setmealDishList = setmealDishMapper.selectBySetmealId(id);

        //创建一个SetmealVO
        SetmealVO setmealVO = new SetmealVO();

        //把两个查询结果封装成一个SetmealVO对象
        BeanUtils.copyProperties(setmeal, setmealVO);
        setmealVO.setSetmealDishes(setmealDishList);

        return setmealVO;
    }

    /**
     * 修改套餐
     *
     * @param setmealDTO
     */
    @Transactional
    @Override
    public void updateWithSetmealDishes(SetmealDTO setmealDTO) {
        //把setmealDTO拆成两部分 套餐对象 和 菜品集合
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.update(setmeal);

        //获取setmealDTO中的套餐Id为菜品集合中的菜品赋值
        Long setmealId = setmeal.getId();

        //获取菜品集合
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        //使用删除 + 插入 的形式代替更新
        setmealDishMapper.deleteBySetmealId(setmealDTO.getId());
        //判断菜品是否为空,不为空才为菜品集合的套餐id赋值
        if (setmealDishes != null && setmealDishes.size() > 0) {
            for (SetmealDish setmealDish : setmealDishes) {
                setmealDish.setSetmealId(setmealId);
            }
            //插入新数据
            setmealDishMapper.insertBatch(setmealDishes);
        }
    }

    /**
     * 批量删除
     *
     * @param ids
     */
    @Transactional
    @Override
    public void deleteBatch(List<Long> ids) {
        //判断套餐是否为启售状态,是则无法删除
        for (Long id : ids) {
            if (setmealMapper.selectById(id).getStatus() == StatusConstant.ENABLE) {
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        }
        //删除数据
        for (Long setmealId : ids) {
            //删除套餐
            setmealMapper.deleteById(setmealId);

            //删除套餐菜品表中对应的数据
            setmealDishMapper.deleteBySetmealId(setmealId);
        }
    }

    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    @Override
    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal> list = setmealMapper.list(setmeal);
        return list;
    }

    /**
     * 根据套餐id查询包含的菜品
     * @param id
     * @return
     */
    @Override
    public List<DishItemVO> getDishItemById(Long id) {
        return setmealMapper.getDishItemBySetmealId(id);
    }
}
