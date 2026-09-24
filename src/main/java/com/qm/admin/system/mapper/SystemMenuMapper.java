package com.qm.admin.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qm.admin.system.entity.SystemMenu;
import com.qm.admin.system.dto.MenuPageItemResponse;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.annotation.InterceptorIgnore;

import java.util.List;

@Mapper
public interface SystemMenuMapper extends BaseMapper<SystemMenu> {

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
            SELECT DISTINCT menu.auth_code
            FROM sys_user_role ur
            JOIN sys_role role ON role.id = ur.role_id
                AND role.tenant_id = ur.tenant_id AND role.status = 1 AND role.deleted = 0
            JOIN sys_role_menu rm ON rm.tenant_id = ur.tenant_id AND rm.role_id = ur.role_id
            JOIN sys_menu menu ON menu.id = rm.menu_id AND menu.status = 1 AND menu.deleted = 0
            WHERE ur.tenant_id = #{tenantId} AND ur.user_id = #{userId}
              AND menu.auth_code IS NOT NULL AND menu.auth_code <> ''
            ORDER BY menu.auth_code
            """)
    List<String> selectAccessCodes(@Param("userId") Long userId, @Param("tenantId") Long tenantId);

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
            SELECT DISTINCT menu.*
            FROM sys_user_role ur
            JOIN sys_role role ON role.id = ur.role_id
                AND role.tenant_id = ur.tenant_id AND role.status = 1 AND role.deleted = 0
            JOIN sys_role_menu rm ON rm.tenant_id = ur.tenant_id AND rm.role_id = ur.role_id
            JOIN sys_menu menu ON menu.id = rm.menu_id AND menu.status = 1 AND menu.deleted = 0
            WHERE ur.tenant_id = #{tenantId} AND ur.user_id = #{userId}
            ORDER BY menu.sort, menu.id
            """)
    List<SystemMenu> selectAuthorizedMenus(@Param("userId") Long userId, @Param("tenantId") Long tenantId);

    @Select("""
            SELECT id, parent_id AS parentId, menu_type AS menuType, name, path, component,
                   redirect, icon, title, sort, status, visible, keep_alive AS keepAlive,
                   affix_tab AS affixTab, auth_code AS authCode, create_time AS createTime,
                   update_time AS updateTime,
                   EXISTS (SELECT 1 FROM sys_menu child WHERE child.parent_id = menu.id AND child.deleted = 0) AS hasChildren
            FROM sys_menu menu
            WHERE menu.parent_id = #{parentId}
              AND menu.deleted = 0
              AND (#{status} IS NULL OR menu.status = #{status})
            ORDER BY menu.sort ASC, menu.id ASC
            """)
    List<MenuPageItemResponse> selectChildren(@Param("parentId") Long parentId,
                                              @Param("status") Integer status);

}
