package com.been.foodieserver.dto.request;

public final class UserRequestValidation {

    //영문 소문자 필수 / 숫자 옵션 / 공백 불가 / 5 ~ 20자
    public static final String LOGIN_ID_PATTERN = "^(?=.*[a-z])[a-z0-9]{5,20}$";
    public static final String LOGIN_ID_MESSAGE = "아이디는 5 ~ 20자 사이여야 합니다. 영문 소문자는 필수, 숫자는 옵션입니다.";

    //영문 소문자, 숫자 포함 필수 / 영문 대문자 옵션 / 공백 불가 / 8 ~ 20자
    public static final String PASSWORD_PATTERN = "^(?=.*[a-z])(?=.*\\d)(?=.*[A-Z]?)[a-zA-Z0-9]{8,20}$";
    public static final String PASSWORD_MESSAGE = "비밀번호는 8 ~ 20자 사이여야 합니다. 영문 소문자, 숫자를 포함해야 하며, 영문 대문자는 옵션입니다.";

    //영문 소문자, 숫자만 가능 / 공백 불가 / 2 ~ 20자
    public static final String NICKNAME_PATTERN = "^[a-z0-9]{2,20}$";
    public static final String NICKNAME_MESSAGE = "닉네임은 2 ~ 20자의 영문 소문자, 숫자만 사용 가능합니다.";

    private UserRequestValidation() {
    }
}
