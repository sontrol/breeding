package com.breeding.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.breeding.entity.User;

public interface UserService extends IService<User> {
    Page<User> getUserPage(int page, int size, String username, String realName);

    boolean deleteUserPermanently(Long userId);
}
