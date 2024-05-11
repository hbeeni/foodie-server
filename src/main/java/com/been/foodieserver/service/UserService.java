package com.been.foodieserver.service;

import com.been.foodieserver.dto.UserDto;
import com.been.foodieserver.exception.CustomException;
import com.been.foodieserver.exception.ErrorCode;
import com.been.foodieserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
@Service
public class UserService {

    private final PasswordEncoder encoder;
    private final UserRepository userRepository;

    public void signUp(UserDto userDto) {
        if (isLoginIdDuplicated(userDto.getLoginId())) {
            throw new CustomException(ErrorCode.DUPLICATE_ID);
        }

        if (isNicknameDuplicated(userDto.getNickname())) {
            throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
        }

        if (!checkPasswordMatch(userDto)) {
            throw new CustomException(ErrorCode.CHECK_PASSWORD);
        }

        userRepository.save(userDto.toEntity(encoder.encode(userDto.getPassword())));
    }

    public boolean isLoginIdDuplicated(String loginId) {
        return userRepository.existsByLoginId(loginId);
    }

    public boolean isNicknameDuplicated(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    private boolean checkPasswordMatch(UserDto userDto) {
        return userDto.getPassword().equals(userDto.getConfirmPassword());
    }
}
