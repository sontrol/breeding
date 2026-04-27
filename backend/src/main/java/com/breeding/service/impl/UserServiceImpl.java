package com.breeding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.breeding.common.UserRoleConstants;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.breeding.entity.User;
import com.breeding.mapper.UserMapper;
import com.breeding.service.UserService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final JdbcTemplate jdbcTemplate;

    public UserServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Page<User> getUserPage(int pageNum, int pageSize, String username, String realName, String roleCode) {
        Page<User> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        
        if (username != null && !username.isEmpty()) {
            wrapper.like(User::getUsername, username);
        }
        if (realName != null && !realName.isEmpty()) {
            wrapper.like(User::getRealName, realName);
        }
        if (roleCode != null && !roleCode.isBlank()) {
            Long roleId = this.baseMapper.selectRoleIdByCode(roleCode);
            if (roleId == null) {
                return new Page<>(pageNum, pageSize, 0);
            }
            wrapper.eq(User::getRoleId, roleId);
        }
        
        wrapper.orderByDesc(User::getId);
        Page<User> result = this.page(page, wrapper);
        result.getRecords().forEach(user -> user.setRoleCode(resolveUserRoleCode(user)));
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveUserWithRole(User user) {
        if (user.getId() == null) {
            prepareNewUser(user);
            Long roleId = resolveRoleId(user.getRoleCode());
            user.setRoleId(roleId);
            return this.save(user);
        }
        validateUpdatableRole(user);
        Long roleId = resolveRoleId(user.getRoleCode());
        user.setRoleId(roleId);
        return this.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUserPermanently(Long userId) {
        jdbcTemplate.update("DELETE FROM user_role WHERE user_id = ?", userId);
        return jdbcTemplate.update("DELETE FROM user WHERE id = ?", userId) > 0;
    }

    private String resolveUserRoleCode(User user) {
        if (user == null || user.getId() == null) {
            return null;
        }
        List<String> roleCodes = this.baseMapper.selectUserRoles(user.getId());
        if (roleCodes != null && !roleCodes.isEmpty()) {
            return roleCodes.get(0);
        }
        return user.getApplyRoleCode();
    }

    private void prepareNewUser(User user) {
        validateAssignableRole(user.getRoleCode());
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        if (user.getAuditStatus() == null) {
            user.setAuditStatus(1);
        }
    }

    private Long resolveRoleId(String roleCode) {
        if (roleCode == null || roleCode.isBlank()) {
            return null;
        }
        Long roleId = this.baseMapper.selectRoleIdByCode(roleCode);
        if (roleId == null) {
            throw new IllegalArgumentException("用户类型不存在");
        }
        return roleId;
    }

    private void validateUpdatableRole(User user) {
        if (user.getRoleCode() == null || user.getRoleCode().isBlank()) {
            return;
        }
        String currentRoleCode = resolveUserRoleCode(this.getById(user.getId()));
        if (UserRoleConstants.ADMIN.equals(currentRoleCode) && UserRoleConstants.ADMIN.equals(user.getRoleCode())) {
            return;
        }
        validateAssignableRole(user.getRoleCode());
    }

    private void validateAssignableRole(String roleCode) {
        if (!UserRoleConstants.isAssignableRole(roleCode)) {
            throw new IllegalArgumentException("该用户类型不允许通过用户管理直接分配");
        }
    }
}
