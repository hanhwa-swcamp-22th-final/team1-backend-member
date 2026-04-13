package com.conk.member.command.application.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LoginRequest {
    private String email;         // 이메일 또는 작업자 코드 (프론트 필드명 email 통일)
    private String password;
}
