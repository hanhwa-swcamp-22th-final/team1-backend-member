package com.conk.member.common.config;

/*
 * Spring Security 설정 클래스다.
 *
 * 여기서 하는 일
 * 1. 세션 대신 JWT를 사용하도록 설정
 * 2. 어떤 URL은 인증 없이 허용할지 결정
 * 3. JWT 필터와 RBAC 권한 필터를 시큐리티 필터 체인에 등록
 * 4. 비밀번호 암호화에 사용할 PasswordEncoder를 빈으로 등록
 */

import com.conk.member.common.jwt.JwtAuthenticationFilter;
import com.conk.member.common.jwt.JwtTokenProvider;
import com.conk.member.common.jwt.RestAccessDeniedHandler;
import com.conk.member.common.jwt.RestAuthenticationEntryPoint;
import com.conk.member.common.security.RolePermissionAuthorizationFilter;
import com.conk.member.common.security.RolePermissionAuthorizationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    private final RestAccessDeniedHandler restAccessDeniedHandler;
    private final RolePermissionAuthorizationService rolePermissionAuthorizationService;

    public SecurityConfig(JwtTokenProvider jwtTokenProvider,
                          UserDetailsService userDetailsService,
                          RestAuthenticationEntryPoint restAuthenticationEntryPoint,
                          RestAccessDeniedHandler restAccessDeniedHandler,
                          RolePermissionAuthorizationService rolePermissionAuthorizationService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
        this.restAuthenticationEntryPoint = restAuthenticationEntryPoint;
        this.restAccessDeniedHandler = restAccessDeniedHandler;
        this.rolePermissionAuthorizationService = rolePermissionAuthorizationService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf
                .ignoringRequestMatchers(
                        "/member/auth/login",
                        "/member/auth/setup-password",
                        "/member/auth/refresh"
                )
        );

        http.sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );

        http.exceptionHandling(exception -> exception
                .authenticationEntryPoint(restAuthenticationEntryPoint)
                .accessDeniedHandler(restAccessDeniedHandler)
        );

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        HttpMethod.POST,
                        "/member/auth/login",
                        "/member/auth/setup-password",
                        "/member/auth/refresh"
                ).permitAll()
                .requestMatchers("/member/admin/**").hasAuthority("SYSTEM_ADMIN")
                .anyRequest().authenticated()
        );

        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        http.addFilterAfter(rolePermissionAuthorizationFilter(), JwtAuthenticationFilter.class);
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

        return http.build();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtTokenProvider, userDetailsService);
    }

    @Bean
    public RolePermissionAuthorizationFilter rolePermissionAuthorizationFilter() {
        return new RolePermissionAuthorizationFilter(rolePermissionAuthorizationService, restAccessDeniedHandler);
    }

    @Bean
    public CorsFilter corsFilter() {
        return new CorsFilter(corsConfigurationSource());
    }

    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOriginPattern("*");
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*");
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
