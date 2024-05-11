package com.been.foodieserver.controller;

import com.been.foodieserver.dto.UserDto;
import com.been.foodieserver.dto.request.UserSignUpRequest;
import com.been.foodieserver.dto.response.ApiResponse;
import com.been.foodieserver.exception.CustomException;
import com.been.foodieserver.exception.ErrorCode;
import com.been.foodieserver.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper mapper;

    @Value("${api.endpoint.base-url}")
    private String baseUrl;

    private String signUpApi;

    @BeforeEach
    void setUp() {
        String requestMapping = baseUrl + "/users";
        signUpApi = requestMapping + "/sign-up";
    }

    @DisplayName("요청이 유효하면 회원가입 성공")
    @Test
    void signUp_IfRequestIsValid() throws Exception {
        //Given
        UserSignUpRequest request = new UserSignUpRequest("loginid", "password12", "password12", "nickname");

        willDoNothing().given(userService).signUp(any(UserDto.class));

        //When & Then
        mockMvc.perform(post(signUpApi).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(ApiResponse.STATUS_SUCCESS));

        verify(userService).signUp(any(UserDto.class));
    }

    @DisplayName("중복된 아이디로 회원가입 시 회원가입 실패")
    @Test
    void failToSignUp_IfLoginIdIsDuplicated() throws Exception {
        //Given
        UserSignUpRequest request = new UserSignUpRequest("duplid", "password12", "password12", "nickname");

        willThrow(new CustomException(ErrorCode.DUPLICATE_ID)).given(userService).signUp(any(UserDto.class));

        //When & Then
        mockMvc.perform(post(signUpApi).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(ApiResponse.STATUS_FAIL))
                .andExpect(jsonPath("$.message").value(ErrorCode.DUPLICATE_ID.getMessage()));

        verify(userService).signUp(any(UserDto.class));
    }

    @DisplayName("중복된 닉네임으로 회원가입 시 회원가입 실패")
    @Test
    void failToSignUp_IfNicknameIsDuplicated() throws Exception {
        //Given
        UserSignUpRequest request = new UserSignUpRequest("loginid", "password12", "password12", "duplnick");

        willThrow(new CustomException(ErrorCode.DUPLICATE_NICKNAME)).given(userService).signUp(any(UserDto.class));

        //When & Then
        mockMvc.perform(post(signUpApi).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(ApiResponse.STATUS_FAIL))
                .andExpect(jsonPath("$.message").value(ErrorCode.DUPLICATE_NICKNAME.getMessage()));

        verify(userService).signUp(any(UserDto.class));
    }
}
