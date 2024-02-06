package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 喜欢悠然独自在
 * @version 1.0
 */
@Service
public class UserServiceImpl implements UserService {

    //微信服务接口地址
    public static final String  WX_LOGIN = "https://api.weixin.qq.com/sns/jscode2session";

    @Autowired
    private WeChatProperties weChatProperties;

    @Autowired
    private UserMapper userMapper;

    /**
     * 微信登录
     * @param userLoginDTO
     * @return
     */
    @Override
    public User wxLogin(UserLoginDTO userLoginDTO) {

        //调用方法通过微信小程序传过来的code获取openid
        String openid = getOpenid(userLoginDTO.getCode());

        //判断微信接口服务返回的openid是否为空,如果为空则登录失败,抛出业务异常
        if (openid == null) {
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }

        //判断该用户是否为新用户
        User user = userMapper.getByOpenid(openid);

        //如果是新用户就把该openid插入到数据库表中
        if (user == null) {
            user = User.builder()
                    .openid(openid)
                    .createTime(LocalDateTime.now())
                    .build();
            userMapper.insert(user);
        }

        //返回这个用户对象
        return user;
    }

    /**
     * 调用微信接口服务,利用微信小程序传过来的code获取openid
     * @param code
     * @return
     */
    private String getOpenid(String code) {
        //调用微信接口服务,获取当前微信用户的唯一标识 -> openid
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("appid",weChatProperties.getAppid());
        paramMap.put("secret",weChatProperties.getSecret());
        paramMap.put("js_code", code);
        paramMap.put("grant_type","authorization_code");
        String json = HttpClientUtil.doGet(WX_LOGIN, paramMap);
        //解析微信接口服务返回的json数据包
        JSONObject jsonObject = JSON.parseObject(json);
        String openid = jsonObject.getString("openid");

        return openid;
    }

}
