package com.been.foodieserver.utils;

import com.been.foodieserver.dto.response.ApiResponse;
import com.been.foodieserver.exception.JwtErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public abstract class JwtUtils {

    private static final ObjectMapper mapper;

    static {
        mapper = new ObjectMapper();
    }

    private JwtUtils() {
    }

    public static void setErrorResponse(HttpServletResponse response, JwtErrorCode code) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(code.getStatus().value());

        ApiResponse<Void> errorResponse = ApiResponse.fail(code.getMessage());
        String s = mapper.writeValueAsString(errorResponse);

        response.getWriter().write(s);
    }
}
