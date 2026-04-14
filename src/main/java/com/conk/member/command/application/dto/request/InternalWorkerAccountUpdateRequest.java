package com.conk.member.command.application.dto.request;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * WMS 내부 작업자 수정 요청 DTO다.
 */
@Getter
@Setter
@NoArgsConstructor
public class InternalWorkerAccountUpdateRequest {

    private String name;
    private String email;
    private String accountStatus;
    private List<String> zones;
    private String memo;
    private String presenceStatus;
}
