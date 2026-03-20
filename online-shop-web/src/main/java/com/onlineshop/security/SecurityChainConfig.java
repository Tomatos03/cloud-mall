package com.onlineshop.security;

import com.onlineshop.framework.security.WhiteListProperties;
import com.onlineshop.framework.security.handler.CustomerAccessDeniedHandler;
import com.onlineshop.framework.security.handler.CustomerAuthenticationEntryPoint;
import com.onlineshop.security.filter.WebTokenAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * 安全链路配置
 *
 * @author : Tomatos
 * @date : 2026/2/2
 */
@Configuration
@RequiredArgsConstructor
public class SecurityChainConfig {
    private final CustomerAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomerAccessDeniedHandler accessDeniedHandler;
    private final WhiteListProperties whiteList;
    private final CorsConfigurationSource corsConfigurationSource;
    private final WebTokenAuthenticationFilter webTokenAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity.securityMatcher("/web/**")
                           .authorizeHttpRequests((authorize) -> {
                               authorize.requestMatchers(HttpMethod.OPTIONS, "/**")
                                        .permitAll()
                                        .requestMatchers(
                                                whiteList.getWhiteList()
                                                         .toArray(new String[0])
                                        )
                                        .permitAll()
                                        .anyRequest()
                                        .authenticated();
                           })
                           .exceptionHandling(exceptionHand -> {
                               exceptionHand.accessDeniedHandler(accessDeniedHandler);
                               exceptionHand.authenticationEntryPoint(authenticationEntryPoint);
                           })
                           .sessionManagement(session -> {
                               session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
                           })
                           .addFilterAfter(webTokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                           .cors(cors -> {
                               cors.configurationSource(corsConfigurationSource);
                           })
                           .csrf(AbstractHttpConfigurer::disable)
                           .build();
    }
}