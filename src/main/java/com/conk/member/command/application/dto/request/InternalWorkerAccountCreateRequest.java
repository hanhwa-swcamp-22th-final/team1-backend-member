package com.conk.member.command.application.dto.request;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * WMS 내부 작업자 생성 요청 DTO다.
 */
@Getter
@Setter
@NoArgsConstructor
public class InternalWorkerAccountCreateRequest {

    private String id;
    private String name;
    private String password;
    private String email;
    private String accountStatus;
    private List<String> zones;
    private String memo;
    private String presenceStatus;
    private String registeredAt;
}
