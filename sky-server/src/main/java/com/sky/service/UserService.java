package com.sky.service;

import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;

/**
 * @author 喜欢悠然独自在
 * @version 1.0
 */
public interface UserService {

    /**
     * 微信登录
     * @param userLoginDTO
     * @return
     */
    User wxLogin(UserLoginDTO userLoginDTO);
}
