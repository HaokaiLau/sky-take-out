package com.sky.exception;

/**
 * @author 喜欢悠然独自在
 * @version 1.0
 * @since 2024/4/25 20:34:45
 * 权限异常
 */
public class PermissionException extends BaseException{

    public PermissionException() {
    }

    public PermissionException(String msg) {
        super(msg);
    }
}
