package com.breeding.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.breeding.common.LoginUser;
import com.breeding.entity.User;
import com.breeding.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        
        List<String> permissions = userMapper.selectUserPermissions(user.getId());
        List<String> roles = userMapper.selectUserRoles(user.getId());
        
        // 将角色也作为权限添加，通常加上 ROLE_ 前缀
        roles.forEach(role -> permissions.add("ROLE_" + role));
        
        return new LoginUser(user, permissions);
    }
}
