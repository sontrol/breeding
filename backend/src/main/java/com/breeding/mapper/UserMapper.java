package com.breeding.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.breeding.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT p.code FROM user u " +
            "LEFT JOIN role_permission rp ON u.role_id = rp.role_id " +
            "LEFT JOIN permission p ON rp.permission_id = p.id " +
            "WHERE u.id = #{userId} AND p.code IS NOT NULL")
    List<String> selectUserPermissions(Long userId);

    @Select("SELECT r.role_code FROM user u " +
            "LEFT JOIN role r ON u.role_id = r.id " +
            "WHERE u.id = #{userId}")
    List<String> selectUserRoles(Long userId);

    @Select("SELECT id FROM role WHERE role_code = #{roleCode} LIMIT 1")
    Long selectRoleIdByCode(String roleCode);
}
