package com.conk.member;

/*
 * 멤버 인증/인가 모듈의 Spring Boot 시작점이다.
 * command / query / common 패키지를 함께 스캔해 애플리케이션을 실행한다.
 */

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class Team1BackendMemberApplication {

    public static void main(String[] args) {
        SpringApplication.run(Team1BackendMemberApplication.class, args);
    }
}
