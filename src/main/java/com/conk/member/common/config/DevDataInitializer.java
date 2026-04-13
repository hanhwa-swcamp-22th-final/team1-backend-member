package com.conk.member.common.config;

import com.conk.member.command.domain.aggregate.Account;
import com.conk.member.command.domain.aggregate.Role;
import com.conk.member.command.domain.aggregate.Seller;
import com.conk.member.command.domain.aggregate.Tenant;
import com.conk.member.command.domain.enums.AccountStatus;
import com.conk.member.command.domain.enums.RoleName;
import com.conk.member.command.domain.enums.SellerStatus;
import com.conk.member.command.domain.repository.AccountRepository;
import com.conk.member.command.domain.repository.RoleRepository;
import com.conk.member.command.domain.repository.SellerRepository;
import com.conk.member.command.domain.repository.TenantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("dev")
public class DevDataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DevDataInitializer.class);

    private static final String CREATED_BY = "system-seed";
    private static final String DEFAULT_PASSWORD = "1234";

    private static final String DEMO_TENANT_ID = "TENANT-DEMO-001";
    private static final String DEMO_TENANT_CODE = "TEN-CONK-DEMO-001";
    private static final String DEMO_SELLER_ID = "SELLER-DEMO-001";
    private static final String DEMO_WAREHOUSE_ID = "WH-001";

    private static final String SYSTEM_ADMIN_ACCOUNT_ID = "ACC-DEMO-001";
    private static final String MASTER_ADMIN_ACCOUNT_ID = "ACC-DEMO-002";
    private static final String WAREHOUSE_MANAGER_ACCOUNT_ID = "ACC-DEMO-003";
    private static final String WAREHOUSE_WORKER_ACCOUNT_ID = "ACC-DEMO-004";
    private static final String SELLER_ACCOUNT_ID = "ACC-DEMO-005";

    private static final String SYSTEM_ADMIN_EMAIL = "sys.admin@conk.com";
    private static final String MASTER_ADMIN_EMAIL = "master.admin@conk.com";
    private static final String WAREHOUSE_MANAGER_EMAIL = "wh.manager@conk.com";
    private static final String SELLER_EMAIL = "seller@conk.com";
    private static final String WAREHOUSE_WORKER_CODE = "WORKER-001";

    private final RoleRepository roleRepository;
    private final TenantRepository tenantRepository;
    private final SellerRepository sellerRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    public DevDataInitializer(RoleRepository roleRepository,
                              TenantRepository tenantRepository,
                              SellerRepository sellerRepository,
                              AccountRepository accountRepository,
                              PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.tenantRepository = tenantRepository;
        this.sellerRepository = sellerRepository;
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        initialize();
    }

    void initialize() {
        ensureRoles();

        Tenant tenant = ensureDemoTenant();
        Seller seller = ensureDemoSeller(tenant);
        String encodedPassword = passwordEncoder.encode(DEFAULT_PASSWORD);

        ensureSystemAdmin(encodedPassword);
        ensureMasterAdmin(tenant, encodedPassword);
        ensureWarehouseManager(tenant, encodedPassword);
        ensureWarehouseWorker(tenant, encodedPassword);
        ensureSellerUser(tenant, seller, encodedPassword);

        log.info("개발용 기본 계정 seed 적용 완료");
    }

    private void ensureRoles() {
        ensureRole("ROLE-001", RoleName.SYSTEM_ADMIN, "플랫폼 전체 사용자 및 업체 관리");
        ensureRole("ROLE-002", RoleName.MASTER_ADMIN, "3PL 업체 운영, 셀러 회사 및 계정 관리");
        ensureRole("ROLE-003", RoleName.WAREHOUSE_MANAGER, "창고 운영, 로케이션·재고·작업자 관리");
        ensureRole("ROLE-004", RoleName.WAREHOUSE_WORKER, "작업자 코드 로그인 기반 현장 작업 처리");
        ensureRole("ROLE-005", RoleName.SELLER, "상품·ASN·주문 관리, 재고 알림 확인");
    }

    private void ensureRole(String roleId, RoleName roleName, String description) {
        roleRepository.findByRoleName(roleName)
                .orElseGet(() -> roleRepository.save(Role.create(roleId, roleName.name(), description, CREATED_BY)));
    }

    private Tenant ensureDemoTenant() {
        return tenantRepository.findById(DEMO_TENANT_ID)
                .orElseGet(() -> {
                    Tenant tenant = Tenant.create(
                            DEMO_TENANT_ID,
                            DEMO_TENANT_CODE,
                            "CONK Demo Logistics",
                            "CONK 운영팀",
                            "123-45-67890",
                            "010-1000-0001",
                            "demo-tenant@conk.com",
                            "Seoul",
                            "GENERAL",
                            CREATED_BY
                    );
                    tenant.activate();
                    return tenantRepository.save(tenant);
                });
    }

    private Seller ensureDemoSeller(Tenant tenant) {
        return sellerRepository.findById(DEMO_SELLER_ID)
                .orElseGet(() -> {
                    Seller seller = new Seller();
                    seller.setSellerId(DEMO_SELLER_ID);
                    seller.setTenantId(tenant.getTenantId());
                    seller.setCustomerCode("CUST-DEMO-001");
                    seller.setSellerInfo("기본 데모 셀러");
                    seller.setBrandNameKo("CONK 데모 셀러");
                    seller.setBrandNameEn("CONK Demo Seller");
                    seller.setRepresentativeName("데모 담당자");
                    seller.setBusinessNo("123-45-67891");
                    seller.setPhoneNo("010-1000-0002");
                    seller.setEmail("demo-seller@conk.com");
                    seller.setCategoryName("데모");
                    seller.setStatus(SellerStatus.ACTIVE);
                    seller.setCreatedBy(CREATED_BY);
                    seller.setUpdatedBy(CREATED_BY);
                    return sellerRepository.save(seller);
                });
    }

    private void ensureSystemAdmin(String encodedPassword) {
        if (accountRepository.existsByEmail(SYSTEM_ADMIN_EMAIL)) {
            return;
        }

        Role role = getRole(RoleName.SYSTEM_ADMIN);
        Account account = Account.createEmailAccount(
                role,
                null,
                null,
                null,
                "시스템 관리자",
                SYSTEM_ADMIN_EMAIL,
                encodedPassword,
                "010-1000-1000",
                AccountStatus.ACTIVE,
                false,
                CREATED_BY
        );
        account.setAccountId(SYSTEM_ADMIN_ACCOUNT_ID);
        accountRepository.save(account);
    }

    private void ensureMasterAdmin(Tenant tenant, String encodedPassword) {
        if (accountRepository.existsByEmail(MASTER_ADMIN_EMAIL)) {
            return;
        }

        Role role = getRole(RoleName.MASTER_ADMIN);
        Account account = Account.createEmailAccount(
                role,
                tenant,
                null,
                null,
                "총괄 관리자",
                MASTER_ADMIN_EMAIL,
                encodedPassword,
                "010-1000-1001",
                AccountStatus.ACTIVE,
                false,
                CREATED_BY
        );
        account.setAccountId(MASTER_ADMIN_ACCOUNT_ID);
        accountRepository.save(account);
    }

    private void ensureWarehouseManager(Tenant tenant, String encodedPassword) {
        if (accountRepository.existsByEmail(WAREHOUSE_MANAGER_EMAIL)) {
            return;
        }

        Role role = getRole(RoleName.WAREHOUSE_MANAGER);
        Account account = Account.createEmailAccount(
                role,
                tenant,
                null,
                DEMO_WAREHOUSE_ID,
                "창고 관리자",
                WAREHOUSE_MANAGER_EMAIL,
                encodedPassword,
                "010-1000-1002",
                AccountStatus.ACTIVE,
                false,
                CREATED_BY
        );
        account.setAccountId(WAREHOUSE_MANAGER_ACCOUNT_ID);
        accountRepository.save(account);
    }

    private void ensureWarehouseWorker(Tenant tenant, String encodedPassword) {
        if (accountRepository.existsByWorkerCode(WAREHOUSE_WORKER_CODE)) {
            return;
        }

        Role role = getRole(RoleName.WAREHOUSE_WORKER);
        Account account = Account.createWorkerAccount(
                role,
                tenant,
                DEMO_WAREHOUSE_ID,
                "창고 작업자",
                WAREHOUSE_WORKER_CODE,
                encodedPassword,
                "010-1000-1003",
                CREATED_BY
        );
        account.setAccountId(WAREHOUSE_WORKER_ACCOUNT_ID);
        accountRepository.save(account);
    }

    private void ensureSellerUser(Tenant tenant, Seller seller, String encodedPassword) {
        if (accountRepository.existsByEmail(SELLER_EMAIL)) {
            return;
        }

        Role role = getRole(RoleName.SELLER);
        Account account = Account.createEmailAccount(
                role,
                tenant,
                seller.getSellerId(),
                null,
                "셀러 담당자",
                SELLER_EMAIL,
                encodedPassword,
                "010-1000-1004",
                AccountStatus.ACTIVE,
                false,
                CREATED_BY
        );
        account.setAccountId(SELLER_ACCOUNT_ID);
        accountRepository.save(account);
    }

    private Role getRole(RoleName roleName) {
        return roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new IllegalStateException("기본 역할을 찾을 수 없습니다: " + roleName));
    }
}
