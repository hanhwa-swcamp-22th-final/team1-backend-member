package com.conk.member.common;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class MemberApiMockDataFactory {

    private MemberApiMockDataFactory() {
    }

    public static Map<String, Object> sellerStats() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("activeSellerCount", 18);
        data.put("newThisMonth", 2);
        data.put("trendType", "up");
        return data;
    }

    public static List<Map<String, Object>> sellerList() {
        List<Map<String, Object>> sellers = new ArrayList<>();
        sellers.add(seller(1L, "한국미용상사", "Korea Beauty Co.", "CUST-001", "김미영", "kim.my@kbeauty.com", "뷰티/화장품", "ACTIVE", LocalDate.of(2026, 1, 10)));
        sellers.add(seller(2L, "테크기어", "TechGear Inc.", "CUST-002", "John Kim", "ops@techgear.com", "전자기기", "ACTIVE", LocalDate.of(2026, 2, 5)));
        return sellers;
    }

    public static List<Map<String, Object>> sellerRevenueList() {
        List<Map<String, Object>> items = new ArrayList<>();
        items.add(sellerRevenue("CUST-001", "TechGear Inc.", 42_000_000, 182, 230_769));
        items.add(sellerRevenue("CUST-002", "Korea Beauty Co.", 18_700_000, 92, 203_260));
        return items;
    }

    public static List<Map<String, Object>> sellerFeeSummaryList() {
        List<Map<String, Object>> items = new ArrayList<>();
        items.add(sellerFeeSummary("CUST-001", "TechGear Inc.", 45_000_000, 5.2, 85));
        items.add(sellerFeeSummary("CUST-002", "Korea Beauty Co.", 19_800_000, 3.7, 73));
        return items;
    }

    public static List<Map<String, Object>> userList() {
        List<Map<String, Object>> users = new ArrayList<>();
        users.add(user(1L, "김창고", "wh.manager@conk.com", "WH_MANAGER", "ACTIVE", "LA West Coast Hub", 1L, null, null, null, LocalDate.of(2025, 12, 1), "2026-03-18 09:12"));
        users.add(user(2L, "홍길동", "seller.manager@conk.com", "SELLER", "INVITED", null, null, "샘플 판매자", "SELLER-001", null, LocalDate.of(2026, 1, 15), null));
        return users;
    }

    public static Map<String, Object> accountStatus(String status) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("accountStatus", status);
        return data;
    }

    public static List<Map<String, Object>> companies() {
        List<Map<String, Object>> items = new ArrayList<>();
        items.add(companyDetail(1L, "TEN-FASTSHIP-001", "FASTSHIP LOGISTICS", "ACTIVE"));
        items.add(companyDetail(2L, "TEN-GLOBAL-002", "GLOBAL WEST 3PL", "SETTING"));
        return items;
    }

    public static Map<String, Object> companyDetail(long id, String tenantCode, String name, String status) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", id);
        item.put("name", name);
        item.put("tenantCode", tenantCode);
        item.put("status", status);
        item.put("createdAt", "2026-03-12T10:12:00");
        item.put("representative", "Sarah Park");
        item.put("businessNumber", "91-4820318");
        item.put("phone", "+1-213-555-1821");
        item.put("email", "ops@fastshiplogistics.com");
        item.put("address", "1850 Alameda St, Los Angeles, CA 90058");
        item.put("companyType", "K-글로벌 전문");
        item.put("warehouseCount", 3);
        item.put("sellerCount", 14);
        item.put("userCount", 27);
        item.put("lockedAccountCount", 2);
        item.put("warehouseList", warehouseList());
        item.put("sellerCompanyList", List.of("Nordic House", "Korea Beauty Co."));
        item.put("activationLinkStatus", "USED");
        return item;
    }

    public static List<Map<String, Object>> adminUsers() {
        List<Map<String, Object>> items = new ArrayList<>();
        items.add(adminUser(101L, 1L, "Sarah Park", "sarah.park@fastship.us", "MASTER_ADMIN", "FASTSHIP LOGISTICS", "-", "ACTIVE", "2026-03-12T10:12:00", "2026-03-13T09:18:00", true));
        items.add(adminUser(102L, 1L, "Alex Han", "alex.han@fastship.us", "SELLER", "FASTSHIP LOGISTICS", "LA Main Hub", "INVITED", "2026-03-14T11:30:00", null, false));
        return items;
    }

    public static List<Map<String, Object>> companyLogs() {
        List<Map<String, Object>> items = new ArrayList<>();
        items.add(companyLog(1L, 1L, "2026-03-12T10:12:00", "sys.admin@conk.com", "업체 등록"));
        items.add(companyLog(2L, 1L, "2026-03-13T09:18:00", "sys.admin@conk.com", "업체 상태 변경"));
        return items;
    }

    public static List<Map<String, Object>> feeProfiles() {
        List<Map<String, Object>> items = new ArrayList<>();
        items.add(feeProfile(1L, "AMAZON_FBM", "Amazon FBM 기본", 15, 0, "USD", "2026-03-10T09:00:00"));
        items.add(feeProfile(2L, "SHOPIFY", "Shopify 기본", 3, 1, "USD", "2026-03-11T10:30:00"));
        return items;
    }

    public static Map<String, Object> feeProfile(long id, String channel, String name, int baseRate, int fixedFee, String currency, String updatedAt) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", id);
        item.put("channel", channel);
        item.put("name", name);
        item.put("baseRate", baseRate);
        item.put("fixedFee", fixedFee);
        item.put("currency", currency);
        item.put("updatedAt", updatedAt);
        return item;
    }

    public static long displayId(String rawId) {
        if (rawId == null || rawId.isBlank()) {
            return 1L;
        }
        String digits = rawId.replaceAll("\\D+", "");
        return digits.isBlank() ? 1L : Long.parseLong(digits);
    }

    private static Map<String, Object> seller(long id, String brandNameKo, String brandNameEn, String customerCode, String contactName, String contactEmail, String category, String status, LocalDate createdAt) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", id);
        item.put("brandNameKo", brandNameKo);
        item.put("brandNameEn", brandNameEn);
        item.put("customerCode", customerCode);
        item.put("warehouses", List.of("LA West Coast Hub", "NJ East Fulfillment"));
        item.put("contactName", contactName);
        item.put("contactEmail", contactEmail);
        item.put("category", category);
        item.put("status", status);
        item.put("createdAt", createdAt.toString());
        return item;
    }

    private static Map<String, Object> sellerRevenue(String sellerCode, String sellerName, int monthRevenue, int totalOrders, int avgOrderValue) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("sellerCode", sellerCode);
        item.put("sellerName", sellerName);
        item.put("monthRevenue", monthRevenue);
        item.put("totalOrders", totalOrders);
        item.put("avgOrderValue", avgOrderValue);
        return item;
    }

    private static Map<String, Object> sellerFeeSummary(String sellerCode, String sellerName, int estimatedCost, double momGrowth, int turnoverRate) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("sellerCode", sellerCode);
        item.put("sellerName", sellerName);
        item.put("estimatedCost", estimatedCost);
        item.put("momGrowth", momGrowth);
        item.put("turnoverRate", turnoverRate);
        return item;
    }

    private static Map<String, Object> user(Long id, String name, String email, String role, String accountStatus, String warehouse, Long warehouseId, String seller, String sellerId, String workerCode, LocalDate createdAt, String lastLogin) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", id);
        item.put("name", name);
        item.put("email", email);
        item.put("role", role);
        item.put("accountStatus", accountStatus);
        item.put("warehouse", warehouse);
        item.put("warehouseId", warehouseId);
        item.put("seller", seller);
        item.put("sellerId", sellerId);
        item.put("workerCode", workerCode);
        item.put("createdAt", createdAt.toString());
        item.put("lastLogin", lastLogin);
        return item;
    }

    private static List<Map<String, Object>> warehouseList() {
        List<Map<String, Object>> items = new ArrayList<>();
        Map<String, Object> warehouse1 = new LinkedHashMap<>();
        warehouse1.put("id", 1L);
        warehouse1.put("code", "LAX-A");
        warehouse1.put("name", "LA Main Hub");
        warehouse1.put("status", "운영중");
        items.add(warehouse1);

        Map<String, Object> warehouse2 = new LinkedHashMap<>();
        warehouse2.put("id", 2L);
        warehouse2.put("code", "LAX-B");
        warehouse2.put("name", "LA Overflow Hub");
        warehouse2.put("status", "운영중");
        items.add(warehouse2);
        return items;
    }

    private static Map<String, Object> adminUser(Long id, Long companyId, String name, String email, String role, String organization, String warehouse, String status, String registeredAt, String lastLoginAt, boolean wasActiveBeforeCompanyInactivation) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", id);
        item.put("companyId", companyId);
        item.put("name", name);
        item.put("email", email);
        item.put("role", role);
        item.put("organization", organization);
        item.put("warehouse", warehouse);
        item.put("status", status);
        item.put("registeredAt", registeredAt);
        item.put("lastLoginAt", lastLoginAt);
        item.put("wasActiveBeforeCompanyInactivation", wasActiveBeforeCompanyInactivation);
        return item;
    }

    private static Map<String, Object> companyLog(Long id, Long companyId, String at, String actor, String action) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", id);
        item.put("companyId", companyId);
        item.put("at", at);
        item.put("actor", actor);
        item.put("action", action);
        return item;
    }
}
