package com.breeding.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.breeding.common.LoginUser;
import com.breeding.entity.User;
import com.breeding.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
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
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new DisabledException("账号已被禁用");
        }
        if (user.getAuditStatus() != null && user.getAuditStatus() == 0) {
            throw new DisabledException("注册申请待审核，请联系牧场主或管理员处理");
        }
        if (user.getAuditStatus() != null && user.getAuditStatus() == 2) {
            throw new DisabledException("注册申请已驳回，请联系审核人员");
        }
        
        List<String> permissions = userMapper.selectUserPermissions(user.getId());
        List<String> roles = userMapper.selectUserRoles(user.getId());

        // 角色同时注入到授权集合，便于后续保留角色兜底判断
        roles.forEach(role -> permissions.add("ROLE_" + role));
        
        return new LoginUser(user, permissions);
    }
}
