package com.breeding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.breeding.dto.user.RegisterAuditDTO;
import com.breeding.entity.Role;
import com.breeding.entity.User;
import com.breeding.mapper.RoleMapper;
import com.breeding.mapper.UserMapper;
import com.breeding.service.RegisterAuditService;
import com.breeding.vo.RegisterAuditVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RegisterAuditServiceImpl implements RegisterAuditService {

    private static final Set<String> ALLOWED_ROLE_CODES = Set.of("owner", "vet", "feeder");

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Override
    public Page<RegisterAuditVO> getPage(int pageNum, int pageSize, String username, String realName, Integer auditStatus, String applyRoleCode) {
        Page<User> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNotNull(User::getApplyRoleCode);

        if (username != null && !username.isEmpty()) {
            wrapper.like(User::getUsername, username);
        }
        if (realName != null && !realName.isEmpty()) {
            wrapper.like(User::getRealName, realName);
        }
        if (auditStatus != null) {
            wrapper.eq(User::getAuditStatus, auditStatus);
        }
        if (applyRoleCode != null && !applyRoleCode.isEmpty()) {
            wrapper.eq(User::getApplyRoleCode, applyRoleCode);
        }

        wrapper.orderByAsc(User::getAuditStatus).orderByDesc(User::getCreateTime);
        Page<User> userPage = userMapper.selectPage(page, wrapper);

        List<RegisterAuditVO> records = userPage.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        Page<RegisterAuditVO> result = new Page<>(pageNum, pageSize, userPage.getTotal());
        result.setRecords(records);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(RegisterAuditDTO auditDTO, Long auditorId) {
        User user = getPendingUser(auditDTO.getUserId());
        String roleCode = user.getApplyRoleCode();
        if (!ALLOWED_ROLE_CODES.contains(roleCode)) {
            throw new IllegalArgumentException("待审核用户的申请角色不合法");
        }

        Long roleId = userMapper.selectRoleIdByCode(roleCode);
        if (roleId == null) {
            throw new IllegalArgumentException("申请角色不存在，无法完成审核");
        }

        userMapper.deleteUserRoles(user.getId());
        userMapper.insertUserRole(user.getId(), roleId);

        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setAuditStatus(1);
        updateUser.setAuditRemark(auditDTO.getAuditRemark());
        updateUser.setAuditBy(auditorId);
        updateUser.setAuditTime(LocalDateTime.now());
        updateUser.setStatus(1);
        userMapper.updateById(updateUser);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(RegisterAuditDTO auditDTO, Long auditorId) {
        User user = getPendingUser(auditDTO.getUserId());

        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setAuditStatus(2);
        updateUser.setAuditRemark(auditDTO.getAuditRemark());
        updateUser.setAuditBy(auditorId);
        updateUser.setAuditTime(LocalDateTime.now());
        userMapper.deleteUserRoles(user.getId());
        userMapper.updateById(updateUser);
    }

    private User getPendingUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null || user.getApplyRoleCode() == null) {
            throw new IllegalArgumentException("注册申请不存在");
        }
        if (!Objects.equals(user.getAuditStatus(), 0)) {
            throw new IllegalArgumentException("该注册申请已处理，不能重复审核");
        }
        return user;
    }

    private RegisterAuditVO toVO(User user) {
        RegisterAuditVO vo = new RegisterAuditVO();
        BeanUtils.copyProperties(user, vo);

        Role role = roleMapper.selectOne(new LambdaQueryWrapper<Role>()
                .eq(Role::getRoleCode, user.getApplyRoleCode())
                .last("LIMIT 1"));
        if (role != null) {
            vo.setApplyRoleName(role.getRoleName());
        }

        if (user.getAuditBy() != null) {
            User auditor = userMapper.selectById(user.getAuditBy());
            if (auditor != null) {
                vo.setAuditByName(auditor.getRealName());
            }
        }
        return vo;
    }
}
