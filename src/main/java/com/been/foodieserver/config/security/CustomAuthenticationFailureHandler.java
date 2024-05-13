package com.been.foodieserver.config.security;

import com.been.foodieserver.dto.response.ApiResponse;
import com.been.foodieserver.exception.CustomException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@RequiredArgsConstructor
@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper mapper;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");

        Throwable cause = exception.getCause();

        if (cause instanceof CustomException customException) {
            response.setStatus(customException.getStatus().value());
            response.getWriter().write(mapper.writeValueAsString(ApiResponse.fail(customException)));
        } else {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write(mapper.writeValueAsString(ApiResponse.fail(exception)));
        }
    }
}
