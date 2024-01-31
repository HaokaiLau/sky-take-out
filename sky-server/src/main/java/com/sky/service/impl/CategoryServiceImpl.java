package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.CategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author 喜欢悠然独自在
 * @version 1.0
 */
@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    /**
     * 新增分类
     * @param categoryDTO
     */
    @Override
    public void save(CategoryDTO categoryDTO) {
        //把categoryDTO中的数据拷贝到category
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO, category);

        //使用aop + 反射为公共字段自动填充
        //设置创建时间,更新时间,和创建者id更新者id
//        category.setCreateTime(LocalDateTime.now());
//        category.setUpdateTime(LocalDateTime.now());
//        category.setCreateUser(BaseContext.getCurrentId());
//        category.setUpdateUser(BaseContext.getCurrentId());

        //分类的默认状态给禁用
        category.setStatus(StatusConstant.DISABLE);

        categoryMapper.insert(category);
    }

    /**
     * 分类分页查询
     *
     * @param categoryPageQueryDTO
     * @return
     */
    @Override
    public PageResult page(CategoryPageQueryDTO categoryPageQueryDTO) {
        //设置分页参数
        PageHelper.startPage(categoryPageQueryDTO.getPage(), categoryPageQueryDTO.getPageSize());

        Page<Category> p = categoryMapper.page(categoryPageQueryDTO);

        long total = p.getTotal();
        List<Category> records = p.getResult();
        PageResult pageResult = new PageResult(total, records);

        return pageResult;
    }

    /**
     * 启用禁用分类
     *
     * @param status
     * @param id
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        Category category = Category.builder()
                .status(status)
                .id(id)
                .build();

        categoryMapper.update(category);
    }

    /**
     * 修改分类
     * @param categoryDTO
     */
    @Override
    public void update(CategoryDTO categoryDTO) {
        Category category = new Category();
        //属性拷贝
        BeanUtils.copyProperties(categoryDTO, category);
        categoryMapper.update(category);
    }

    /**
     * 根据类型查询分类
     *
     * @param type
     * @return
     */
    @Override
    public List<Category> list(Integer type) {
        return categoryMapper.list(type);
    }

    /**
     * 根据id删除分类
     *
     * @param id
     */
    @Override
    public void deleteById(Long id) {
        //根据id查询该分类下是否存在菜品,有就抛出业务异常,没有才能删除
        Integer count = dishMapper.countByCategoryId(id);
        if (count > 0) {
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_DISH);
        }
        //根据id查询该分类下是否存在菜品,有就抛出业务异常,没有才能删除
        count = setmealMapper.countByCategoryId(id);
        if (count > 0) {
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_SETMEAL);
        }

        //删除分类数据
        categoryMapper.deleteById(id);
    }
}
