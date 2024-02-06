package com.sky.controller.user;

import com.sky.constant.StatusConstant;
import com.sky.entity.Setmeal;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author 喜欢悠然独自在
 * @version 1.0
 */
@RestController("userSetmealController")
@Slf4j
@RequestMapping("/user/setmeal")
@Api(tags = "C端-套餐浏览接口")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    /**
     * 根据分类id查询套餐
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询套餐")
    public Result<List<Setmeal>> list(Long categoryId) {
        log.info("根据分类id查询套餐:{}",categoryId);
        Setmeal setmeal = Setmeal.builder()
                .categoryId(categoryId)
                .status(StatusConstant.ENABLE)//查询启售中的套餐
                .build();

        List<Setmeal> list = setmealService.list(setmeal);
        return Result.success(list);
    }

    /**
     * 根据套餐id查询菜品
     * @param id
     * @return
     */
    @GetMapping("dish/{id}")
    @ApiOperation("根据套餐id查询菜品")
    public Result<List<DishItemVO>> dishList(@PathVariable Long id) {
        log.info("根据套餐id查询菜品:{}",id);
        List<DishItemVO> list = setmealService.getDishItemById(id);
        return Result.success(list);
    }

}
