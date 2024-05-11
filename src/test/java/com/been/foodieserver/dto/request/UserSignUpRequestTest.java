package com.been.foodieserver.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class UserSignUpRequestTest {

    private static Validator validator;

    private static final String NOT_BLANK = "{jakarta.validation.constraints.NotBlank.message}";
    private static final String SIZE = "{jakarta.validation.constraints.Size.message}";

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @DisplayName("아이디: 영문 소문자 필수 / 숫자 옵션 / 공백 불가 / 5 ~ 20자")
    @MethodSource
    @ParameterizedTest(name = "[{0}]")
    void validateLoginId(String loginId, Set<String> expectedViolationMessageTemplates) {
        //Given
        UserSignUpRequest request = new UserSignUpRequest(loginId, "password12", "password12", "nick");

        //When
        Set<ConstraintViolation<UserSignUpRequest>> violations = validator.validate(request);

        Set<String> actualViolationMessageTemplates = violations.stream()
                .filter(v -> v.getPropertyPath().toString().equals("loginId"))
                .map(ConstraintViolation::getMessageTemplate)
                .collect(Collectors.toSet());

        //Then
        assertThat(actualViolationMessageTemplates).containsExactlyInAnyOrderElementsOf(expectedViolationMessageTemplates);
    }

    @DisplayName("비밀번호: 영문 소문자, 숫자 포함 필수 / 영문 대문자 옵션 / 공백 불가 / 8 ~ 20자")
    @MethodSource
    @ParameterizedTest(name = "[{0}]")
    void validatePassword(String password, Set<String> expectedViolationMessageTemplates) {
        //Given
        UserSignUpRequest request = new UserSignUpRequest("loginId", password, "password12", "nick");

        //When
        Set<ConstraintViolation<UserSignUpRequest>> violations = validator.validate(request);

        Set<String> actualViolationMessageTemplates = violations.stream()
                .filter(v -> v.getPropertyPath().toString().equals("password"))
                .map(ConstraintViolation::getMessageTemplate)
                .collect(Collectors.toSet());

        //Then
        assertThat(actualViolationMessageTemplates).containsExactlyInAnyOrderElementsOf(expectedViolationMessageTemplates);
    }

    @DisplayName("닉네임: 영문 소문자, 숫자만 가능 / 공백 불가 / 2 ~ 20자")
    @MethodSource
    @ParameterizedTest(name = "[{0}]")
    void validateNickname(String nickname, Set<String> expectedViolationMessageTemplates) {
        //Given
        UserSignUpRequest request = new UserSignUpRequest("loginId", "password12", "password12", nickname);

        //When
        Set<ConstraintViolation<UserSignUpRequest>> violations = validator.validate(request);

        Set<String> actualViolationMessageTemplates = violations.stream()
                .filter(v -> v.getPropertyPath().toString().equals("nickname"))
                .map(ConstraintViolation::getMessageTemplate)
                .collect(Collectors.toSet());

        //Then
        assertThat(actualViolationMessageTemplates).containsExactlyInAnyOrderElementsOf(expectedViolationMessageTemplates);
    }

    static Stream<Arguments> validateLoginId() {
        String pattern = "아이디는 5 ~ 20자 사이여야 합니다. 영문 소문자는 필수, 숫자는 옵션입니다.";

        return Stream.of(
                arguments("abc123", Set.of()),
                arguments("hello987", Set.of()),
                arguments("hello", Set.of()),
                arguments("abcdefabcdefabcdefab", Set.of()),
                arguments(null, Set.of(NOT_BLANK)),
                arguments("", Set.of(NOT_BLANK, SIZE, pattern)),
                arguments("   ", Set.of(NOT_BLANK, SIZE, pattern)),
                arguments("a", Set.of(SIZE, pattern)),
                arguments("a  ", Set.of(SIZE, pattern)),
                arguments("a  b", Set.of(SIZE, pattern)),
                arguments("A", Set.of(SIZE, pattern)),
                arguments("ab", Set.of(SIZE, pattern)),
                arguments("abcdefabcdefabcdefabc", Set.of(SIZE, pattern)),
                arguments("123456789012345678901", Set.of(SIZE, pattern)),
                arguments("12345678", Set.of(pattern)),
                arguments("a  bb", Set.of(pattern)),
                arguments("Abcdef", Set.of(pattern)),
                arguments("Abcdef12!", Set.of(pattern)),
                arguments("UPPERCASE", Set.of(pattern)),
                arguments("abc123!", Set.of(pattern)),
                arguments("abc123_", Set.of(pattern)),
                arguments("abc123/", Set.of(pattern)),
                arguments("abc123\\", Set.of(pattern))
        );
    }

    static Stream<Arguments> validatePassword() {
        String pattern = "비밀번호는 8 ~ 20자 사이여야 합니다. 영문 소문자, 숫자를 포함해야 하며, 영문 대문자는 옵션입니다.";

        return Stream.of(
                arguments("password1", Set.of()),
                arguments("password12", Set.of()),
                arguments("password12password12", Set.of()),
                arguments("Password123", Set.of()),
                arguments("StrongPW789", Set.of()),
                arguments("Test1234", Set.of()),
                arguments("SuperSecurePass99", Set.of()),
                arguments(null, Set.of(NOT_BLANK)),
                arguments("", Set.of(NOT_BLANK, SIZE, pattern)),
                arguments("   ", Set.of(NOT_BLANK, SIZE, pattern)),
                arguments("p", Set.of(SIZE, pattern)),
                arguments("p  ", Set.of(SIZE, pattern)),
                arguments("P", Set.of(SIZE, pattern)),
                arguments("short", Set.of(SIZE, pattern)),
                arguments("ThisIsTooLong123456789", Set.of(SIZE, pattern)),
                arguments("NoNumbersButLongEnoughAndUpperCase", Set.of(SIZE, pattern)),
                arguments("ALLUPPERCASE123", Set.of(pattern)),
                arguments("p      w", Set.of(pattern)),
                arguments("password", Set.of(pattern)),
                arguments("Password", Set.of(pattern)),
                arguments("P1234567", Set.of(pattern)),
                arguments("thisHasNoNumbers", Set.of(pattern)),
                arguments("secureP@ssw0rd", Set.of(pattern))
        );
    }

    static Stream<Arguments> validateNickname() {
        String pattern = "닉네임은 2 ~ 20자의 영문 소문자, 숫자만 사용 가능합니다.";

        return Stream.of(
                arguments("ab12", Set.of()),
                arguments("hello", Set.of()),
                arguments("example4567", Set.of()),
                arguments("test12345", Set.of()),
                arguments("alpha0001", Set.of()),
                arguments("1a2b3c4d5", Set.of()),
                arguments("9876abcde", Set.of()),
                arguments("qwertyuiop1234567890", Set.of()),
                arguments("12345", Set.of()),
                arguments(null, Set.of(NOT_BLANK)),
                arguments("", Set.of(NOT_BLANK, SIZE, pattern)),
                arguments(" ", Set.of(NOT_BLANK, SIZE, pattern)),
                arguments("p", Set.of(SIZE, pattern)),
                arguments("ThisIsAReallyLongString", Set.of(SIZE, pattern)),
                arguments("thisisareallylongstring", Set.of(SIZE, pattern)),
                arguments("ABCD", Set.of(pattern)),
                arguments("123 456", Set.of(pattern)),
                arguments("ALLUPPERCASE", Set.of(pattern)),
                arguments("ALLUPPERCASE123", Set.of(pattern)),
                arguments("hello world", Set.of(pattern)),
                arguments("nick!", Set.of(pattern)),
                arguments("Nick", Set.of(pattern)),
                arguments("Nick!", Set.of(pattern)),
                arguments("specialchar@", Set.of(pattern))
        );
    }
}
