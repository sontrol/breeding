package com.breeding.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.breeding.entity.User;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    
    @Select("SELECT p.code FROM user u " +
            "LEFT JOIN user_role ur ON u.id = ur.user_id " +
            "LEFT JOIN role_permission rp ON ur.role_id = rp.role_id " +
            "LEFT JOIN permission p ON rp.permission_id = p.id " +
            "WHERE u.id = #{userId} AND p.code IS NOT NULL")
    List<String> selectUserPermissions(Long userId);
    
    @Select("SELECT r.role_code FROM user u " +
            "LEFT JOIN user_role ur ON u.id = ur.user_id " +
            "LEFT JOIN role r ON ur.role_id = r.id " +
            "WHERE u.id = #{userId}")
    List<String> selectUserRoles(Long userId);

    @Select("SELECT id FROM role WHERE role_code = #{roleCode} LIMIT 1")
    Long selectRoleIdByCode(String roleCode);

    @Insert("INSERT INTO user_role (user_id, role_id) VALUES (#{userId}, #{roleId})")
    int insertUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);

    @Delete("DELETE FROM user_role WHERE user_id = #{userId}")
    int deleteUserRoles(Long userId);
}
