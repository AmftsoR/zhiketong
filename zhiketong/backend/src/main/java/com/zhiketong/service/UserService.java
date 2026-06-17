package com.zhiketong.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhiketong.entity.User;

/**
 * 用户服务接口
 */
public interface UserService extends IService<User> {

    /**
     * 登录 - 验证用户名密码，成功返回用户（不含密码）
     */
    User login(String username, String password);

    /**
     * 注册新用户（密码会加密存储）
     */
    boolean register(User user);
}
