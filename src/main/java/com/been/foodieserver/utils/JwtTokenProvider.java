package com.been.foodieserver.utils;

import com.been.foodieserver.dto.CustomUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtTokenProvider {

    private static final String ID_KEY = "loginId";
    private static final String ROLE_KEY = "role";

    private final UserDetailsService userDetailsService;
    private Key key;

    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.expiration-time}")
    private int expirationTime;

    @PostConstruct
    public void init() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String createToken(Authentication authentication) {
        log.info("create token");

        CustomUserDetails userDetails = (CustomUserDetails) (authentication.getPrincipal());
        String loginId = userDetails.getLoginId();
        String roleName = userDetails.getRole().getRoleName();

        long now = (new Date()).getTime();
        Date validity = new Date(now + expirationTime);
        log.info("validity = {}", validity);

        return Jwts.builder()
                .setSubject(loginId)
                .claim(ID_KEY, loginId)
                .claim(ROLE_KEY, roleName)
                .signWith(key, SignatureAlgorithm.HS512)
                .setExpiration(validity)
                .compact();
    }

    public Authentication getAuthentication(String token) {
        Claims claims = Jwts
                .parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return new UsernamePasswordAuthenticationToken(
                userDetailsService.loadUserByUsername(claims.get(ID_KEY).toString()),
                token,
                Set.of(new SimpleGrantedAuthority(claims.get(ROLE_KEY).toString()))
        );
    }
}
