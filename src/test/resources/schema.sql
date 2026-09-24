DROP TABLE IF EXISTS sys_auth_token;
DROP TABLE IF EXISTS sys_dict_item;
DROP TABLE IF EXISTS sys_dict;
DROP TABLE IF EXISTS sys_role_menu;
DROP TABLE IF EXISTS sys_user_role;
DROP TABLE IF EXISTS sys_user_tenant;
DROP TABLE IF EXISTS sys_app;
DROP TABLE IF EXISTS sys_menu;
DROP TABLE IF EXISTS sys_role;
DROP TABLE IF EXISTS sys_user;
DROP TABLE IF EXISTS sys_tenant;

CREATE TABLE sys_tenant (
    id BIGINT PRIMARY KEY, tenant_code VARCHAR(64) NOT NULL, tenant_name VARCHAR(128) NOT NULL,
    tenant_type VARCHAR(16) NOT NULL, contact_name VARCHAR(64), contact_phone VARCHAR(32), status INT NOT NULL,
    expire_time TIMESTAMP, remark VARCHAR(500), create_time TIMESTAMP, update_time TIMESTAMP,
    create_by BIGINT, update_by BIGINT, deleted INT
);
CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY, username VARCHAR(64) NOT NULL, password_hash VARCHAR(100) NOT NULL,
    real_name VARCHAR(64) NOT NULL, avatar VARCHAR(512), description VARCHAR(255), home_path VARCHAR(255),
    status INT NOT NULL, create_time TIMESTAMP, update_time TIMESTAMP, create_by BIGINT, update_by BIGINT, deleted INT
);
CREATE TABLE sys_app (
    id BIGINT PRIMARY KEY, tenant_id BIGINT NOT NULL, appid VARCHAR(64) NOT NULL, app_code VARCHAR(64) NOT NULL,
    app_name VARCHAR(128) NOT NULL, app_type VARCHAR(32) NOT NULL, status INT NOT NULL, remark VARCHAR(500),
    create_time TIMESTAMP, update_time TIMESTAMP, create_by BIGINT, update_by BIGINT, deleted INT
);
CREATE TABLE sys_user_tenant (
    id BIGINT PRIMARY KEY, tenant_id BIGINT NOT NULL, user_id BIGINT NOT NULL, is_tenant_admin INT NOT NULL,
    status INT NOT NULL, create_time TIMESTAMP, update_time TIMESTAMP, create_by BIGINT, update_by BIGINT
);
CREATE TABLE sys_role (
    id BIGINT PRIMARY KEY, tenant_id BIGINT NOT NULL, role_code VARCHAR(64) NOT NULL, role_name VARCHAR(64) NOT NULL,
    status INT NOT NULL, remark VARCHAR(500), create_time TIMESTAMP, update_time TIMESTAMP,
    create_by BIGINT, update_by BIGINT, deleted INT
);
CREATE TABLE sys_dict (
    id BIGINT PRIMARY KEY, tenant_id BIGINT NOT NULL, dict_name VARCHAR(64) NOT NULL, dict_code VARCHAR(64) NOT NULL,
    status INT NOT NULL, remark VARCHAR(500), create_time TIMESTAMP, update_time TIMESTAMP, create_by BIGINT, update_by BIGINT, deleted INT
);
CREATE TABLE sys_dict_item (
    id BIGINT PRIMARY KEY, tenant_id BIGINT NOT NULL, dict_id BIGINT NOT NULL, item_name VARCHAR(64) NOT NULL, item_code VARCHAR(64) NOT NULL,
    sort INT NOT NULL, color VARCHAR(32), icon VARCHAR(128), status INT NOT NULL, create_time TIMESTAMP, update_time TIMESTAMP, create_by BIGINT, update_by BIGINT, deleted INT
);
CREATE TABLE sys_menu (
    id BIGINT PRIMARY KEY, parent_id BIGINT NOT NULL, menu_type VARCHAR(16) NOT NULL, name VARCHAR(100) NOT NULL,
    path VARCHAR(255), component VARCHAR(255), redirect VARCHAR(255), icon VARCHAR(100), title VARCHAR(128),
    sort INT, status INT, visible INT, keep_alive INT, affix_tab INT, auth_code VARCHAR(128),
    create_time TIMESTAMP, update_time TIMESTAMP, create_by BIGINT, update_by BIGINT, deleted INT
);
CREATE TABLE sys_user_role (
    id BIGINT PRIMARY KEY, tenant_id BIGINT NOT NULL, user_id BIGINT NOT NULL, role_id BIGINT NOT NULL,
    create_time TIMESTAMP, create_by BIGINT
);
CREATE TABLE sys_role_menu (
    id BIGINT PRIMARY KEY, tenant_id BIGINT NOT NULL, role_id BIGINT NOT NULL, menu_id BIGINT NOT NULL,
    create_time TIMESTAMP, create_by BIGINT
);
CREATE TABLE sys_auth_token (
    id BIGINT PRIMARY KEY, tenant_id BIGINT NOT NULL, user_id BIGINT NOT NULL, access_token_hash VARCHAR(64) NOT NULL,
    refresh_token_hash VARCHAR(64) NOT NULL, access_expires_time TIMESTAMP NOT NULL,
    refresh_expires_time TIMESTAMP NOT NULL, revoked_time TIMESTAMP, create_time TIMESTAMP,
    update_time TIMESTAMP, create_by BIGINT, update_by BIGINT, deleted INT
);
