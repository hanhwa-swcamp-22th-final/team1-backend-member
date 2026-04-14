package com.conk.member.query.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

/**
 * WMS 내부 작업자 응답 DTO다.
 */
@Getter
@Builder
public class InternalWorkerAccountResponse {

    private String id;
    private String name;
    private String email;
    private String accountStatus;
    private List<String> zones;
    private String memo;
    private String presenceStatus;
    private String registeredAt;
}
