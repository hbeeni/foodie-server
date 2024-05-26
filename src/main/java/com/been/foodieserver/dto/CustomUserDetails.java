package com.been.foodieserver.dto;

import com.been.foodieserver.domain.Role;
import com.been.foodieserver.domain.User;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CustomUserDetails implements UserDetails {

    private static final String ROLE_PREFIX = "ROLE_";

    private Long id;
    private String loginId;
    private String password;
    private String nickname;
    private Role role;

    public static CustomUserDetails from(User user) {
        return new CustomUserDetails(user.getId(), user.getLoginId(), user.getPassword(), user.getNickname(), user.getRole());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Set.of(new SimpleGrantedAuthority(ROLE_PREFIX + role.getRoleName()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return loginId;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
