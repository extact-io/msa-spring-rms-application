package io.extact.msa.spring.domain.user.model;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

import io.extact.msa.spring.rms.domain.user.model.UserType;

class UserTypeTest {

    @Test
    void testIsAmdin() {
        assertThat(UserType.ADMIN.isAdmin()).isTrue();
        assertThat(UserType.MEMBER.isAdmin()).isFalse();
    }

    @Test
    void testIsValidUserType() {
        assertThat(UserType.isValidUserType("ADMIN")).isTrue();
        assertThat(UserType.isValidUserType("MEMBER")).isTrue();
        assertThat(UserType.isValidUserType("admin")).isFalse();
        assertThat(UserType.isValidUserType("member")).isFalse();
    }
}
