package com.zhiketong.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhiketong.entity.User;
import com.zhiketong.mapper.UserMapper;
import com.zhiketong.service.UserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public User login(String username, String password) {
        // 先按用户名查找
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User user = this.getOne(wrapper);

        if (user == null) {
            return null;
        }

        // BCrypt 比对密码
        if (!encoder.matches(password, user.getPassword())) {
            return null;
        }

        // 清除密码后返回
        user.setPassword(null);
        return user;
    }

    @Override
    public boolean register(User user) {
        // 密码加密
        user.setPassword(encoder.encode(user.getPassword()));
        // 默认角色
        if (user.getRole() == null) {
            user.setRole("student");
        }
        user.setCreatedAt(null); // 由数据库自动生成
        return this.save(user);
    }
}
