package com.been.foodieserver.controller;

import com.been.foodieserver.domain.Role;
import com.been.foodieserver.domain.User;
import com.been.foodieserver.dto.CustomUserDetails;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
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

    @Autowired
    private PasswordEncoder encoder;

    @Value("${api.endpoint.base-url}")
    private String baseUrl;

    private String signUpApi;
    private String loginApi;
    private String logoutApi;

    @BeforeEach
    void setUp() {
        String requestMapping = baseUrl + "/users";
        signUpApi = requestMapping + "/sign-up";
        loginApi = requestMapping + "/login";
        logoutApi = requestMapping + "/logout";
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

    @DisplayName("아이디, 비밀번호가 유효하면 로그인 성공")
    @Test
    void login_IfRequestIsValid() throws Exception {
        //Given
        String loginId = "loginid";
        String password = "password12";

        User user = User.builder()
                .loginId(loginId)
                .password(encoder.encode(password))
                .role(Role.USER)
                .build();
        CustomUserDetails customUserDetails = CustomUserDetails.from(user);

        given(userService.searchUser(loginId)).willReturn(Optional.of(customUserDetails));

        //When & Then
        mockMvc.perform(post(loginApi).with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .accept(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("loginId", loginId)
                        .param("password", password))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(ApiResponse.STATUS_SUCCESS));

        then(userService).should().searchUser(loginId);
    }

    @DisplayName("회원가입하지 않은 아이디 또는 틀린 비밀번호로 로그인 시 로그인 실패")
    @Test
    void failToLogin_IfUserNotSignedUp() throws Exception {
        //Given
        String loginId = "loginid";
        String password = "password12";

        given(userService.searchUser(loginId)).willReturn(Optional.empty());

        //When & Then
        mockMvc.perform(post(loginApi).with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .accept(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("loginId", loginId)
                        .param("password", password))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(ApiResponse.STATUS_FAIL))
                .andExpect(jsonPath("$.message").value(ErrorCode.USER_NOT_FOUND.getMessage()));

        then(userService).should().searchUser(loginId);
    }

    @DisplayName("로그아웃 성공")
    @Test
    void logout() throws Exception {
        //Given

        //When & Then
        mockMvc.perform(post(logoutApi).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(ApiResponse.STATUS_SUCCESS));
    }
}
