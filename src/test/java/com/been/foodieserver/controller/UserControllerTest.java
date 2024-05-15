package com.been.foodieserver.controller;

import com.been.foodieserver.domain.Role;
import com.been.foodieserver.domain.User;
import com.been.foodieserver.dto.CustomUserDetails;
import com.been.foodieserver.dto.UserDto;
import com.been.foodieserver.dto.request.UserInfoModifyRequest;
import com.been.foodieserver.dto.request.UserPasswordChangeRequest;
import com.been.foodieserver.dto.request.UserSignUpRequest;
import com.been.foodieserver.dto.response.ApiResponse;
import com.been.foodieserver.dto.response.UserInfoResponse;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

    private String userApi;
    private String signUpApi;
    private String loginApi;
    private String logoutApi;
    private String myInfoApi;
    private String passwordApi;

    @BeforeEach
    void setUp() {
        userApi = baseUrl + "/users";
        signUpApi = userApi + "/sign-up";
        loginApi = userApi + "/login";
        logoutApi = userApi + "/logout";
        myInfoApi = userApi + "/my";
        passwordApi = userApi + "/my/password";
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

        User user = User.of(loginId, encoder.encode(password), null, Role.USER);
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

    @DisplayName("로그인이 되어 있으면 내 정보 조회 성공")
    @Test
    void getMyInfo_IfLoggedIn() throws Exception {
        //Given
        String loginId = "user1";
        User user = User.of(loginId, null, "nickname", Role.USER);

        UserInfoResponse userInfoResponse = UserInfoResponse.my(user);

        given(userService.getMyInfo(loginId)).willReturn(userInfoResponse);

        //When & Then
        mockMvc.perform(get(myInfoApi)
                        .with(user(loginId).roles(Role.USER.getRoleName()))
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(ApiResponse.STATUS_SUCCESS))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.loginId").value(user.getLoginId()))
                .andExpect(jsonPath("$.data.nickname").value(user.getNickname()))
                .andExpect(jsonPath("$.data.role").value(user.getRole().getRoleName()))
                .andExpect(jsonPath("$.data.loginId").value(loginId));

        then(userService).should().getMyInfo(loginId);
    }

    @DisplayName("로그인이 되어 있지 않으면 내 정보 조회 실패")
    @Test
    void failToGetMyInfo_IfNotLoggedIn() throws Exception {
        //Given

        //When & Then
        mockMvc.perform(get(myInfoApi)
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8"))
                .andExpect(status().is(ErrorCode.AUTH_FAIL.getStatus().value()))
                .andExpect(jsonPath("$.status").value(ApiResponse.STATUS_FAIL))
                .andExpect(jsonPath("$.message").value(ErrorCode.AUTH_FAIL.getMessage()));

        then(userService).shouldHaveNoInteractions();
    }

    @WithMockUser
    @DisplayName("유저 아이디가 존재하면 유저 정보 조회 성공")
    @Test
    void getUserInfo_IfUserLoginIdExists() throws Exception {
        //Given
        String loginId = "others";
        User user = User.of(loginId, null, "nickname", Role.USER);

        UserInfoResponse userInfoResponse = UserInfoResponse.others(user);

        given(userService.getUserInfo(loginId)).willReturn(userInfoResponse);

        //When & Then
        mockMvc.perform(get(userApi + "/" + user.getLoginId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(ApiResponse.STATUS_SUCCESS))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.loginId").value(user.getLoginId()))
                .andExpect(jsonPath("$.data.nickname").value(user.getNickname()))
                .andExpect(jsonPath("$.data.role").doesNotExist())
                .andExpect(jsonPath("$.data.createdAt").doesNotExist())
                .andExpect(jsonPath("$.data.modifiedAt").doesNotExist())
                .andExpect(jsonPath("$.data.deletedAt").doesNotExist());

        then(userService).should().getUserInfo(loginId);
    }

    @WithMockUser
    @DisplayName("유저 아이디가 존재하지 않으면 유저 정보 조회 실패")
    @Test
    void failToGetUserInfo_IfUserLoginIdDoesntExist() throws Exception {
        //Given
        String loginId = "someid";

        given(userService.getUserInfo(loginId)).willThrow(new CustomException(ErrorCode.USER_NOT_FOUND));

        //When & Then
        mockMvc.perform(get(userApi + "/" + loginId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(ErrorCode.USER_NOT_FOUND.getStatus().value()))
                .andExpect(jsonPath("$.status").value(ApiResponse.STATUS_FAIL))
                .andExpect(jsonPath("$.message").value(ErrorCode.USER_NOT_FOUND.getMessage()));

        then(userService).should().getUserInfo(loginId);
    }

    @WithMockUser
    @DisplayName("수정할 정보 요청이 유효하면 정보 수정 성공")
    @Test
    void modifyMyInfo_IfRequestIsValid() throws Exception {
        //Given
        String loginId = "user";
        String nickname = "nickname";

        UserInfoModifyRequest request = new UserInfoModifyRequest(nickname);
        User user = User.of(loginId, null, nickname, Role.USER);
        UserInfoResponse response = UserInfoResponse.my(user);

        given(userService.modifyMyInfo(eq(loginId), any(UserDto.class))).willReturn(response);

        //When & Then
        mockMvc.perform(put(myInfoApi)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(ApiResponse.STATUS_SUCCESS))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.loginId").value(user.getLoginId()))
                .andExpect(jsonPath("$.data.nickname").value(user.getNickname()))
                .andExpect(jsonPath("$.data.role").value(user.getRole().getRoleName()))
                .andExpect(jsonPath("$.data.loginId").value(loginId));

        then(userService).should().modifyMyInfo(eq(loginId), any(UserDto.class));
    }

    @WithMockUser
    @DisplayName("비밀번호  요청이 유효하면 비밀번호 변경 성공")
    @Test
    void changePassword_IfRequestIsValid() throws Exception {
        //Given
        String loginId = "user";
        String currentPassword = "current123";
        String newPassword = "newpasswd123";

        UserPasswordChangeRequest request = new UserPasswordChangeRequest(currentPassword, newPassword, newPassword);

        willDoNothing().given(userService).changePassword(loginId, currentPassword, newPassword, newPassword);

        //When & Then
        mockMvc.perform(put(passwordApi)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(ApiResponse.STATUS_SUCCESS))
                .andExpect(jsonPath("$.data").doesNotExist());

        then(userService).should().changePassword(loginId, currentPassword, newPassword, newPassword);
    }
}
