package com.sky.controller.user;

import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.properties.JwtProperties;
import com.sky.result.Result;
import com.sky.service.UserService;
import com.sky.utils.JwtUtil;
import com.sky.vo.UserLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 喜欢悠然独自在
 * @version 1.0
 */
@RestController
@Slf4j
@RequestMapping("/user/user")
@Api(tags = "C端用户相关接口")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 微信登录
     * @return
     */
    @PostMapping("/login")
    @ApiOperation("微信登录")
    public Result<UserLoginVO> login(@RequestBody UserLoginDTO userLoginDTO){
        log.info("微信登录:{}",userLoginDTO);

        //微信登录
        User user = userService.wxLogin(userLoginDTO);

        //生成jwt令牌
        Map<String, Object> claims = new HashMap<>();//生成jwt令牌的第二部分内容
        claims.put(JwtClaimsConstant.USER_ID,user.getId());//把从前端微信获取到的code加入到集合中
        String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);

        //把数据封装成一个UserLoginVO对象
        UserLoginVO userLoginVO = UserLoginVO.builder()
                .id(user.getId())
                .openid(user.getOpenid())
                .token(token)
                .build();

        return Result.success(userLoginVO);
    }

}
