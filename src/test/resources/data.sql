INSERT INTO sys_tenant (id, tenant_code, tenant_name, tenant_type, status, deleted)
VALUES (1, 'TENANT_A', '租户 A', 'SINGLE', 1, 0),
       (2, 'TENANT_B', '租户 B', 'CHAIN', 1, 0),
       (3, 'TENANT_C', '租户 C', 'SINGLE', 1, 0);

INSERT INTO sys_user (id, username, password_hash, real_name, avatar, description, home_path, status, deleted)
VALUES (1, 'admin', '$2y$10$UwwTtNxMhfIuqygtbyUwAOoBIVfUuP.due9uMmJ2FOyPuQvqRWUOG', '管理员', '', '系统管理员', '/dashboard/workspace', 1, 0),
       (2, 'multiadmin', '$2y$10$UwwTtNxMhfIuqygtbyUwAOoBIVfUuP.due9uMmJ2FOyPuQvqRWUOG', '多租户管理员', '', '多租户账号', '/system/user', 1, 0),
       (3, 'operator', '$2y$10$UwwTtNxMhfIuqygtbyUwAOoBIVfUuP.due9uMmJ2FOyPuQvqRWUOG', '运营人员', '', '租户 A 普通成员', '/system/user', 0, 0);

INSERT INTO sys_app (id, tenant_id, appid, app_code, app_name, app_type, status, deleted)
VALUES (1, 1, 'wx_tenant_a', 'APP_A', '租户 A 小程序', 'WECHAT_MINI_APP', 1, 0),
       (2, 2, 'wx_tenant_b', 'APP_B', '租户 B 小程序', 'WECHAT_MINI_APP', 1, 0);

INSERT INTO sys_user_tenant (id, tenant_id, user_id, is_tenant_admin, status)
VALUES (1, 1, 1, 1, 1),
       (2, 1, 2, 1, 1),
       (3, 2, 2, 1, 1),
       (4, 1, 3, 0, 0);

INSERT INTO sys_role (id, tenant_id, role_code, role_name, status, deleted)
VALUES (1, 1, 'admin', '租户 A 管理员', 1, 0),
       (2, 2, 'chain_admin', '租户 B 管理员', 1, 0);

INSERT INTO sys_menu (id, parent_id, menu_type, name, path, component, redirect, icon, title, sort, status, visible, keep_alive, affix_tab, auth_code, deleted)
VALUES (1, 0, 'CATALOG', 'SystemManagement', '/system', 'BasicLayout', '/system/user', 'lucide:settings', '系统管理', -1, 1, 1, 0, 0, NULL, 0),
       (2, 1, 'MENU', 'UserManagement', 'user', '/system/user/index', NULL, 'lucide:user-round', '用户管理', 1, 1, 1, 1, 0, NULL, 0),
       (3, 1, 'MENU', 'RoleManagement', 'role', '/system/role/index', NULL, 'lucide:shield-check', '角色管理', 2, 1, 1, 1, 0, NULL, 0),
       (4, 1, 'MENU', 'MenuManagement', 'menu', '/system/menu/index', NULL, 'lucide:menu', '菜单管理', 3, 1, 1, 1, 0, NULL, 0),
       (17, 1, 'MENU', 'DictManagement', 'dict', '/system/dict/index', NULL, 'lucide:book-open', '字典管理', 4, 1, 1, 1, 0, NULL, 0),
       (5, 2, 'BUTTON', 'UserManagementView', NULL, NULL, NULL, NULL, '用户管理查看', 1, 1, 0, 0, 0, 'system:user:view', 0),
       (8, 2, 'BUTTON', 'UserManagementCreate', NULL, NULL, NULL, NULL, '用户管理新增', 2, 1, 0, 0, 0, 'system:user:create', 0),
       (9, 2, 'BUTTON', 'UserManagementUpdate', NULL, NULL, NULL, NULL, '用户管理编辑', 3, 1, 0, 0, 0, 'system:user:update', 0),
       (10, 2, 'BUTTON', 'UserManagementDelete', NULL, NULL, NULL, NULL, '用户管理删除', 4, 1, 0, 0, 0, 'system:user:delete', 0),
       (6, 3, 'BUTTON', 'RoleManagementView', NULL, NULL, NULL, NULL, '角色管理查看', 1, 1, 0, 0, 0, 'system:role:view', 0),
       (11, 3, 'BUTTON', 'RoleManagementCreate', NULL, NULL, NULL, NULL, '角色管理新增', 2, 1, 0, 0, 0, 'system:role:create', 0),
       (12, 3, 'BUTTON', 'RoleManagementUpdate', NULL, NULL, NULL, NULL, '角色管理编辑', 3, 1, 0, 0, 0, 'system:role:update', 0),
       (13, 3, 'BUTTON', 'RoleManagementDelete', NULL, NULL, NULL, NULL, '角色管理删除', 4, 1, 0, 0, 0, 'system:role:delete', 0),
       (7, 4, 'BUTTON', 'MenuManagementView', NULL, NULL, NULL, NULL, '菜单管理查看', 1, 1, 0, 0, 0, 'system:menu:view', 0),
       (14, 4, 'BUTTON', 'MenuManagementCreate', NULL, NULL, NULL, NULL, '菜单管理新增', 2, 1, 0, 0, 0, 'system:menu:create', 0),
       (15, 4, 'BUTTON', 'MenuManagementUpdate', NULL, NULL, NULL, NULL, '菜单管理编辑', 3, 1, 0, 0, 0, 'system:menu:update', 0),
       (16, 4, 'BUTTON', 'MenuManagementDelete', NULL, NULL, NULL, NULL, '菜单管理删除', 4, 1, 0, 0, 0, 'system:menu:delete', 0);
INSERT INTO sys_menu (id, parent_id, menu_type, name, path, component, redirect, icon, title, sort, status, visible, keep_alive, affix_tab, auth_code, deleted)
VALUES (18, 17, 'BUTTON', 'DictManagementView', NULL, NULL, NULL, NULL, '字典管理查看', 1, 1, 0, 0, 0, 'system:dict:view', 0),
       (19, 17, 'BUTTON', 'DictManagementCreate', NULL, NULL, NULL, NULL, '字典管理新增', 2, 1, 0, 0, 0, 'system:dict:create', 0),
       (20, 17, 'BUTTON', 'DictManagementUpdate', NULL, NULL, NULL, NULL, '字典管理编辑', 3, 1, 0, 0, 0, 'system:dict:update', 0),
       (21, 17, 'BUTTON', 'DictManagementDelete', NULL, NULL, NULL, NULL, '字典管理删除', 4, 1, 0, 0, 0, 'system:dict:delete', 0),
       (22, 1, 'BUTTON', 'AccessCacheClear', NULL, NULL, NULL, NULL, '清理鉴权缓存', 5, 1, 0, 0, 0, 'system:cache:clear', 0);

INSERT INTO sys_user_role (id, tenant_id, user_id, role_id)
VALUES (1, 1, 1, 1), (2, 1, 2, 1), (3, 2, 2, 2);

INSERT INTO sys_role_menu (id, tenant_id, role_id, menu_id)
VALUES (1, 1, 1, 1), (2, 1, 1, 2), (3, 1, 1, 3), (4, 1, 1, 4),
       (5, 1, 1, 5), (13, 1, 1, 8), (14, 1, 1, 9), (15, 1, 1, 10),
       (6, 1, 1, 6), (7, 1, 1, 7),
       (17, 1, 1, 11), (18, 1, 1, 12), (19, 1, 1, 13),
       (20, 1, 1, 14), (21, 1, 1, 15), (22, 1, 1, 16), (23, 1, 1, 17), (24, 1, 1, 18),
       (25, 1, 1, 19), (26, 1, 1, 20), (27, 1, 1, 21), (28, 1, 1, 22),
       (8, 2, 2, 1), (9, 2, 2, 2), (10, 2, 2, 3), (11, 2, 2, 4),
       (12, 2, 2, 6), (16, 2, 2, 5);
