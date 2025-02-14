package io.extact.msa.spring.rms.test;

import java.util.Set;
import java.util.stream.Stream;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import io.extact.msa.spring.platform.core.auth.RmsAuthentication;
import io.extact.msa.spring.platform.core.auth.client.RmsClientAuthenticationToken;
import io.extact.msa.spring.platform.core.jwt.encode.JsonWebTokenGenerator;
import io.extact.msa.spring.platform.core.jwt.encode.UserClaims;

public class TestAuthUtils {

    public static Authentication signinByHeader(int id, String... roles) {
        RmsAuthentication testAuth = TestRmsAuthentication.builder()
                .userId(id)
                .roles(Set.of(roles))
                .build();
        SecurityContextHolder.getContext().setAuthentication(testAuth);
        return testAuth;
    }

    public static Authentication signinByHeaderWithRolePrefix(int id, String... roles) {
        return signinByHeader(id, withRolePrefix(roles));
    }

    public static Authentication signinByJwt(JsonWebTokenGenerator generator, int id, String... roles) {

        String bearerToken = generateToken(generator, id, roles);

        RmsClientAuthenticationToken tokenAuth = RmsClientAuthenticationToken.builder()
                .userId(String.valueOf(id))
                .groups(Set.of(roles)) // ROLE_は内部で追加される
                .bearerToken(bearerToken)
                .build();
        SecurityContextHolder.getContext().setAuthentication(tokenAuth);
        return tokenAuth;
    }

    public static void signout(boolean quietly) {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            if (!quietly) {
                throw new IllegalStateException("Not signed in.");
            }
            return;
        }
        SecurityContextHolder.clearContext();
    }

    public static void signoutQuietly() {
        signout(true);
    }

    public static String generateToken(JsonWebTokenGenerator generator, int id, String... roles) {

        UserClaims userClaims = new UserClaims() {
            @Override
            public String userId() {
                return String.valueOf(id);
            }
            @Override
            public String principalName() {
                return id + "@rms.com";
            }
            @Override
            public Set<String> groups() {
                return Set.of(roles);
            }
        };

        return generator.generateToken(userClaims);
    }

    private static String[] withRolePrefix(String... roles) {
        return Stream.of(roles)
                .map("ROLE_"::concat)
                .toArray(String[]::new);
    }
}
