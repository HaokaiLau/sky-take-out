package com.sky.controller.user;

import com.sky.constant.StatusConstant;
import com.sky.entity.Dish;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author 喜欢悠然独自在
 * @version 1.0
 */
@RestController("userDishController")
@Slf4j
@RequestMapping("/user/dish")
@Api(tags = "C端-菜品浏览接口")
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private RedisTemplate redisTemplate;//用于向redis中存取数据

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<DishVO>> list(Long categoryId) {
        log.info("根据分类id查询菜品:{}",categoryId);

        //构造redis中的key,规则:dish_分类id
        String key = "dish_" + categoryId;

        //查询redis中是否存在菜品数据
        //redis的存取规则,当时数据是以什么类型存进去,就用什么类型取出来
        List<DishVO> list = (List<DishVO>) redisTemplate.opsForValue().get(key);
        if (list != null && list.size() > 0) {
            //存在则直接从redis返回
            return Result.success(list);
        }

        //不存在则从mysql数据库中查询数据,并将数据缓存到redis中
        Dish dish = Dish.builder()
                .categoryId(categoryId)
                .status(StatusConstant.ENABLE)//仅查询启售菜品
                .build();
        list = dishService.listWitnFlavor(dish);
        redisTemplate.opsForValue().set(key,list);//把查询出来的数据缓存到redis中

        return Result.success(list);
    }

}
