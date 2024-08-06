package com.been.foodieserver.config.security;

import com.been.foodieserver.exception.JwtErrorCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.been.foodieserver.utils.JwtUtils.setErrorResponse;

@Slf4j
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        log.error("토큰이 존재하지 않거나 Bearer로 시작하지 않습니다. uri = {}", request.getRequestURI());
        setErrorResponse(response, JwtErrorCode.NOT_FOUND_TOKEN);
    }
}
