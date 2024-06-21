package com.been.foodieserver.config;

import com.been.foodieserver.config.security.CustomAuthenticationEntryPoint;
import com.been.foodieserver.config.security.CustomAuthenticationFailureHandler;
import com.been.foodieserver.config.security.CustomAuthenticationSuccessHandler;
import com.been.foodieserver.config.security.CustomLogoutSuccessHandler;
import com.been.foodieserver.domain.Role;
import com.been.foodieserver.exception.CustomException;
import com.been.foodieserver.exception.ErrorCode;
import com.been.foodieserver.service.UserService;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity
@Configuration
public class SecurityConfig {

    private static final String[] WHITE_LIST = {
            "/api/*/users/sign-up", "/api/*/users/id/exists", "/api/*/users/nickname/exists", "/manage/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, CustomAuthenticationSuccessHandler authenticationSuccessHandler,
                                                   CustomAuthenticationFailureHandler authenticationFailureHandler, CustomLogoutSuccessHandler logoutSuccessHandler,
                                                   CustomAuthenticationEntryPoint authenticationEntryPoint) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(form -> form
                        .loginProcessingUrl("/api/v1/users/login")
                        .usernameParameter("loginId")
                        .passwordParameter("password")
                        .successHandler(authenticationSuccessHandler)
                        .failureHandler(authenticationFailureHandler))
                .logout(logout -> logout
                        .logoutUrl("/api/v1/users/logout")
                        .logoutSuccessHandler(logoutSuccessHandler))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                        .requestMatchers(WHITE_LIST).permitAll()
                        .requestMatchers("/refresh/**").hasRole(Role.ADMIN.getRoleName())
                        .anyRequest().authenticated())
                .exceptionHandling(config -> config
                        .authenticationEntryPoint(authenticationEntryPoint))
                .build();
    }

    @Bean
    public UserDetailsService userDetailsService(UserService userService) {
        return loginId -> userService.searchUser(loginId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
