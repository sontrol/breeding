package com.breeding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.breeding.entity.User;
import com.breeding.mapper.UserMapper;
import com.breeding.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public Page<User> getUserPage(int pageNum, int pageSize, String username, String realName) {
        Page<User> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        
        if (username != null && !username.isEmpty()) {
            wrapper.like(User::getUsername, username);
        }
        if (realName != null && !realName.isEmpty()) {
            wrapper.like(User::getRealName, realName);
        }
        
        wrapper.orderByDesc(User::getCreateTime);
        return this.page(page, wrapper);
    }
}
