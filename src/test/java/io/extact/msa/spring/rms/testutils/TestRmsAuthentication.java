package io.extact.msa.spring.rms.testutils;

import java.util.Collection;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;

import io.extact.msa.spring.platform.core.auth.LoginUser;
import io.extact.msa.spring.platform.core.auth.RmsAuthentication;
import lombok.Builder;
import lombok.Singular;

@Builder
public class TestRmsAuthentication implements RmsAuthentication {

    private final int userId;
    @Singular
    private final Set<String> roles;
    @Builder.Default
    private boolean authenticated = true;

    @Override
    public boolean isAuthenticated() {
        return this.authenticated;
    }

    @Override
    public LoginUser getLoginUser() {
        return LoginUser.of(userId, roles);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getCredentials() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getDetails() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getPrincipal() {
        throw new UnsupportedOperationException();
    }


    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException();
    }
}
