package com.conk.member.common.config;

import feign.Logger;
import feign.Request;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * member 서비스 내부통신용 Feign 기본 설정이다.
 */
@Configuration
public class FeignConfig {

    @Bean
    public Request.Options feignRequestOptions() {
        return new Request.Options(5, TimeUnit.SECONDS, 5, TimeUnit.SECONDS, true);
    }

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }
}
