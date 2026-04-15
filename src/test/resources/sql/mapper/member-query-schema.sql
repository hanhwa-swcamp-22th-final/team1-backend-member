-- 테스트용 인메모리 DB 스키마 (H2 MODE=MySQL)
-- 엔티티 구조(ERD 기준)에 맞게 정렬

DROP TABLE IF EXISTS company_log;
DROP TABLE IF EXISTS role_permission_history;
DROP TABLE IF EXISTS role_permission;
DROP TABLE IF EXISTS permission;
DROP TABLE IF EXISTS seller_warehouse;
DROP TABLE IF EXISTS seller;
DROP TABLE IF EXISTS account;
DROP TABLE IF EXISTS invitation;
DROP TABLE IF EXISTS token;
DROP TABLE IF EXISTS tenant;
DROP TABLE IF EXISTS role;

CREATE TABLE role (
    role_id VARCHAR(50) PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL,
    role_description VARCHAR(255),
    is_active INTEGER,
    created_at TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP,
    updated_by VARCHAR(50)
);

CREATE TABLE tenant (
    tenant_id VARCHAR(50) PRIMARY KEY,
    tenant_code VARCHAR(100) NOT NULL,
    tenant_name VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL,
    representative_name VARCHAR(100),
    business_no VARCHAR(50),
    phone_no VARCHAR(50),
    email VARCHAR(100),
    address VARCHAR(255),
    tenant_type VARCHAR(50),
    activated_at TIMESTAMP,
    created_at TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP,
    updated_by VARCHAR(50)
);

CREATE TABLE seller (
    seller_id VARCHAR(50) PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    customer_code VARCHAR(50) NOT NULL,
    seller_info VARCHAR(255),
    brand_name_ko VARCHAR(100),
    brand_name_en VARCHAR(100),
    representative_name VARCHAR(100),
    business_no VARCHAR(50),
    phone_no VARCHAR(50),
    email VARCHAR(100),
    category_name VARCHAR(100),
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP,
    updated_by VARCHAR(50)
);

CREATE TABLE seller_warehouse (
    seller_warehouse_id VARCHAR(50) PRIMARY KEY,
    seller_id VARCHAR(50) NOT NULL,
    tenant_id VARCHAR(50) NOT NULL,
    warehouse_id VARCHAR(50) NOT NULL
);

CREATE TABLE account (
    account_id VARCHAR(50) PRIMARY KEY,
    role_id VARCHAR(50) NOT NULL,
    tenant_id VARCHAR(50),
    seller_id VARCHAR(50),
    warehouse_id VARCHAR(50),
    account_name VARCHAR(100) NOT NULL,
    account_status VARCHAR(50) NOT NULL,
    email VARCHAR(100),
    password_hash VARCHAR(255),
    worker_code VARCHAR(100),
    phone_no VARCHAR(50),
    last_login_at TIMESTAMP,
    password_changed_at TIMESTAMP,
    is_temporary_password BOOLEAN,
    created_at TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP,
    updated_by VARCHAR(50)
);

CREATE TABLE invitation (
    invitation_id VARCHAR(50) PRIMARY KEY,
    inviter_account_id VARCHAR(50),
    invitee_account_id VARCHAR(50),
    target_role_id VARCHAR(50) NOT NULL,
    seller_id VARCHAR(50),
    tenant_id VARCHAR(50),
    warehouse_id VARCHAR(50),
    invite_status VARCHAR(50) NOT NULL,
    invite_sent_at TIMESTAMP,
    invite_expired_at TIMESTAMP,
    invite_email VARCHAR(100) NOT NULL,
    created_at TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP,
    updated_by VARCHAR(50)
);

CREATE TABLE token (
    token_id VARCHAR(50) PRIMARY KEY,
    account_id VARCHAR(50) NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    token_type VARCHAR(50) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    is_used BOOLEAN
);

CREATE TABLE permission (
    permission_id VARCHAR(50) PRIMARY KEY,
    permission_name VARCHAR(100) NOT NULL,
    menu_name VARCHAR(100),
    permission_description VARCHAR(255),
    created_at TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP,
    updated_by VARCHAR(50)
);

-- role_permission: 단일 PK (엔티티 ERD 기준, @GeneratedValue UUID)
CREATE TABLE role_permission (
    role_permission_pk VARCHAR(50) PRIMARY KEY,
    role_id VARCHAR(50) NOT NULL,
    permission_id VARCHAR(50) NOT NULL,
    is_enabled INTEGER,
    can_read INTEGER,
    can_write INTEGER,
    can_delete INTEGER,
    created_at TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP,
    updated_by VARCHAR(50)
);

-- role_permission_history: 엔티티와 API 명세를 함께 검증할 수 있는 구조
CREATE TABLE role_permission_history (
    history_id VARCHAR(50) PRIMARY KEY,
    role_permission_id VARCHAR(50),
    role_id VARCHAR(50) NOT NULL,
    permission_id VARCHAR(50) NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    before_is_enabled INTEGER,
    before_can_read INTEGER,
    before_can_write INTEGER,
    before_can_delete INTEGER,
    after_can_read INTEGER,
    after_can_write INTEGER,
    after_can_delete INTEGER,
    changed_by VARCHAR(100),
    changed_at TIMESTAMP
);


CREATE TABLE company_log (
    company_log_id VARCHAR(50) PRIMARY KEY,
    account_id VARCHAR(50),
    tenant_id VARCHAR(50) NOT NULL,
    actor_name VARCHAR(100) NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    action_summary VARCHAR(255) NOT NULL,
    action_detail VARCHAR(255),
    created_at TIMESTAMP
);
