package com.breeding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.breeding.common.UserRoleConstants;
import com.breeding.dto.auth.RegisterDTO;
import com.breeding.entity.User;
import com.breeding.mapper.UserMapper;
import com.breeding.service.RegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterServiceImpl implements RegisterService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submit(RegisterDTO registerDTO) {
        String roleCode = registerDTO.getRoleCode();
        if (!UserRoleConstants.isRegisterRole(roleCode)) {
            throw new IllegalArgumentException("申请的用户类型不合法");
        }

        User exists = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, registerDTO.getUsername()));
        if (exists != null) {
            if (Integer.valueOf(2).equals(exists.getAuditStatus())) {
                exists.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
                exists.setRealName(registerDTO.getRealName());
                exists.setPhone(registerDTO.getPhone());
                exists.setStatus(1);
                exists.setApplyRoleCode(roleCode);
                exists.setAuditStatus(0);
                exists.setAuditRemark(null);
                exists.setAuditBy(null);
                exists.setAuditTime(null);
                exists.setRoleId(null);
                userMapper.updateById(exists);
                return;
            }
            throw new IllegalArgumentException("用户名已存在，请更换后重新提交");
        }

        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setRealName(registerDTO.getRealName());
        user.setPhone(registerDTO.getPhone());
        user.setStatus(1);
        user.setApplyRoleCode(roleCode);
        user.setAuditStatus(0);
        userMapper.insert(user);
    }
}
