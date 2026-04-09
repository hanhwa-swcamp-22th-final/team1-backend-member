package com.conk.member.common.config;

/*
 * Spring Security 설정 클래스다.
 *
 * 변경 포인트
 * 1. JWT 무상태 인증이므로 csrf disable은 유지
 * 2. CORS 허용 대상을 * 가 아니라 프론트 도메인으로 제한
 * 3. OPTIONS preflight 요청은 허용
 * 4. 별도 CorsFilter 빈은 제거하고 Security의 cors 설정만 사용
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
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

  private static final List<String> ALLOWED_ORIGINS = List.of(
      "http://localhost:3000",
      "http://127.0.0.1:3000",
      "https://your-frontend-domain.com"
  );

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

    // JWT Authorization 헤더 기반 무상태 API이므로 CSRF 비활성화
    http.csrf(AbstractHttpConfigurer::disable);

    http.sessionManagement(session ->
        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
    );

    http.exceptionHandling(exception -> exception
        .authenticationEntryPoint(restAuthenticationEntryPoint)
        .accessDeniedHandler(restAccessDeniedHandler)
    );

    http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

    http.authorizeHttpRequests(auth -> auth
        // 브라우저 preflight 요청 허용
        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

        // 인증 없이 허용할 API
        .requestMatchers(
            HttpMethod.POST,
            "/member/auth/login",
            "/member/auth/setup-password",
            "/member/auth/refresh"
        ).permitAll()

        // 관리자 전용
        .requestMatchers("/member/admin/**").hasAuthority("SYSTEM_ADMIN")

        // 나머지는 인증 필요
        .anyRequest().authenticated()
    );

    http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
    http.addFilterAfter(rolePermissionAuthorizationFilter(), JwtAuthenticationFilter.class);

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
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();

    // 프론트 주소만 허용
    configuration.setAllowedOrigins(ALLOWED_ORIGINS);

    // 필요한 메서드만 허용
    configuration.setAllowedMethods(List.of(
        "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
    ));

    // 필요한 헤더만 허용
    configuration.setAllowedHeaders(List.of(
        "Authorization",
        "Content-Type",
        "X-Requested-With",
        "Accept",
        "Origin"
    ));

    // JWT를 Authorization 헤더로만 보낼 거면 false가 더 안전하다
    configuration.setAllowCredentials(false);

    configuration.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}