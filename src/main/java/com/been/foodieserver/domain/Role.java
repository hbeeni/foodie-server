package com.been.foodieserver.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {

    USER("USER"), ADMIN("admin");

    private final String roleName;
}
