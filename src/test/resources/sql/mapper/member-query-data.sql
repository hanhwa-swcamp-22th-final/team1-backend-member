INSERT INTO role(role_id, role_name, role_description, is_active, created_at) VALUES
('ROLE-001', 'MASTER_ADMIN',       'master admin',       1, CURRENT_TIMESTAMP),
('ROLE-002', 'WAREHOUSE_MANAGER',  'warehouse manager',  1, CURRENT_TIMESTAMP),
('ROLE-003', 'WAREHOUSE_WORKER',   'warehouse worker',   1, CURRENT_TIMESTAMP),
('ROLE-004', 'SELLER',             'seller user',        1, CURRENT_TIMESTAMP),
('ROLE-005', 'SYSTEM_ADMIN',       'system admin',       1, CURRENT_TIMESTAMP);

INSERT INTO tenant(tenant_id, tenant_code, tenant_name, status, representative_name, business_no, phone_no, email, address, tenant_type, activated_at, created_at) VALUES
('TENANT-001', 'TEN-FASTSHIP-001', 'FASTSHIP LOGISTICS', 'ACTIVE',   'Sarah Park', '91-4820318',  '010-1111-2222', 'ops@fastship.com',  'LA',   'K_GLOBAL', TIMESTAMP '2026-04-01 10:00:00', TIMESTAMP '2026-04-01 09:00:00'),
('TENANT-002', 'TEN-KBEAUTY-001',  'K BEAUTY HUB',       'SETTING',  'Kim Mina',   '82-1111111',  '010-2222-3333', 'hello@kbeauty.com', 'Seoul','GENERAL',  NULL,                           TIMESTAMP '2026-04-02 10:00:00');

INSERT INTO seller(seller_id, tenant_id, customer_code, seller_info, brand_name_ko, brand_name_en, representative_name, business_no, phone_no, email, category_name, status, created_at) VALUES
('SELLER-001', 'TENANT-001', 'CUST-001', '미국 서부향 K-Beauty 셀러', '한국미용상사', 'K Beauty House', '김미영', '123-45-67890', '010-1111-2222', 'ops@kbeauty.com', '뷰티/화장품', 'ACTIVE',   TIMESTAMP '2026-04-01 13:00:00'),
('SELLER-002', 'TENANT-001', 'CUST-002', '생활용품 셀러',             '리빙샵',       'Living Shop',    '박리빙', '999-11-11111', '010-3333-4444', 'living@shop.com', '생활',       'INACTIVE', TIMESTAMP '2026-03-28 09:30:00'),
('SELLER-003', 'TENANT-002', 'CUST-003', '패션 셀러',                 '패션하우스',   'Fashion House',  '최패션', '888-22-22222', '010-5555-6666', 'fashion@house.com','패션',      'ACTIVE',   TIMESTAMP '2026-04-02 11:10:00');

INSERT INTO seller_warehouse(seller_warehouse_id, seller_id, tenant_id, warehouse_id) VALUES
('SW-001', 'SELLER-001', 'TENANT-001', 'WH-001'),
('SW-002', 'SELLER-001', 'TENANT-001', 'WH-002'),
('SW-003', 'SELLER-002', 'TENANT-001', 'WH-003');

INSERT INTO account(account_id, role_id, tenant_id, seller_id, warehouse_id, account_name, account_status, email, worker_code, phone_no, last_login_at, password_changed_at, is_temporary_password, created_at) VALUES
('ACC-001', 'ROLE-001', 'TENANT-001', NULL,        NULL,     '총괄관리자',   'ACTIVE',   'master@conk.com',  NULL,         '010-1000-1000', TIMESTAMP '2026-04-03 09:10:00', TIMESTAMP '2026-04-01 10:00:00', FALSE, TIMESTAMP '2026-04-01 09:00:00'),
('ACC-002', 'ROLE-002', 'TENANT-001', NULL,        'WH-001', '창고관리자',   'ACTIVE',   'manager@conk.com', NULL,         '010-2000-2000', TIMESTAMP '2026-04-03 09:20:00', TIMESTAMP '2026-04-01 11:00:00', FALSE, TIMESTAMP '2026-04-01 11:00:00'),
('ACC-003', 'ROLE-003', 'TENANT-001', NULL,        'WH-001', '현장작업자1',  'ACTIVE',   NULL,               'WORKER-001', '010-3000-3000', TIMESTAMP '2026-04-03 08:50:00', TIMESTAMP '2026-04-01 12:00:00', FALSE, TIMESTAMP '2026-04-01 12:00:00'),
('ACC-004', 'ROLE-004', 'TENANT-001', 'SELLER-001',NULL,     '셀러담당자',   'ACTIVE',   'seller@conk.com',  NULL,         '010-4000-4000', TIMESTAMP '2026-04-03 09:00:00', TIMESTAMP '2026-04-01 13:00:00', FALSE, TIMESTAMP '2026-04-01 13:00:00'),
('ACC-005', 'ROLE-002', 'TENANT-002', NULL,        'WH-002', '다른업체관리자','INACTIVE', 'other@conk.com',  NULL,         '010-5000-5000', NULL,                            NULL,                           FALSE, TIMESTAMP '2026-04-02 14:00:00');

INSERT INTO permission(permission_id, permission_name, menu_name, created_at) VALUES
('PERM-001', '사용자 관리',   'users',    CURRENT_TIMESTAMP),
('PERM-002', '재고 관리',     'inventory',CURRENT_TIMESTAMP),
('PERM-003', '작업 관리',     'work',     CURRENT_TIMESTAMP);

-- role_permission: UUID PK 직접 지정
INSERT INTO role_permission(role_permission_pk, role_id, permission_id, is_enabled, can_read, can_write, can_delete, created_at) VALUES
('RP-001', 'ROLE-002', 'PERM-001', 1, 1, 1, 0, CURRENT_TIMESTAMP),
('RP-002', 'ROLE-002', 'PERM-002', 1, 1, 1, 0, CURRENT_TIMESTAMP),
('RP-003', 'ROLE-003', 'PERM-003', 1, 1, 0, 0, CURRENT_TIMESTAMP);

INSERT INTO role_permission_history(history_id, role_permission_id, role_id, permission_id, action_type, before_is_enabled, before_can_read, before_can_write, before_can_delete, after_can_read, after_can_write, after_can_delete, changed_by, changed_at) VALUES
('HIS-001', 'RP-001', 'ROLE-002', 'PERM-001', 'UPDATE', 1, 1, 0, 0, 1, 1, 0, 'master@conk.com', TIMESTAMP '2026-04-03 14:00:00'),
('HIS-002', 'RP-002', 'ROLE-002', 'PERM-002', 'CREATE', 0, 0, 0, 0, 1, 1, 0, 'master@conk.com', TIMESTAMP '2026-04-03 13:50:00'),
('HIS-003', 'RP-003', 'ROLE-003', 'PERM-003', 'UPDATE', 1, 1, 0, 0, 1, 0, 0, 'master@conk.com', TIMESTAMP '2026-04-03 13:40:00');
