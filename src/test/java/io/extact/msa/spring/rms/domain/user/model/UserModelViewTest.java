package io.extact.msa.spring.rms.domain.user.model;

import static io.extact.msa.spring.rms.PersistedTestData.*;
import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

import io.extact.msa.spring.rms.domain.user.model.User.UserCreatable;

class UserModelViewTest {

    private static final UserCreatable testCreater = new UserCreatable() {
    };

    @Test
    void testIsEqualNull() {
        assertThat(user1.isEqual(null)).isFalse();
    }

    @Test
    void testIsEqual() {
        assertThat(user1.isEqual(testCreater.newInstance(
                null,
                null,
                null,
                null,
                null,
                null,
                null)))
                        .isFalse();
        assertThat(user1.isEqual(testCreater.newInstance(
                user1.getId(),
                null,
                null,
                null,
                null,
                null,
                null)))
                        .isFalse();
        assertThat(user1.isEqual(testCreater.newInstance(
                user1.getId(),
                user1.getLoginId(),
                null,
                null,
                null,
                null,
                null)))
                        .isFalse();
        assertThat(user1.isEqual(testCreater.newInstance(
                user1.getId(),
                user1.getLoginId(),
                user1.getPassword(),
                null,
                null,
                null,
                null)))
                        .isFalse();
        assertThat(user1.isEqual(testCreater.newInstance(
                user1.getId(),
                user1.getLoginId(),
                user1.getPassword(),
                user1.getUserType(),
                null,
                null,
                null)))
                        .isFalse();
        assertThat(user1.isEqual(testCreater.newInstance(
                user1.getId(),
                user1.getLoginId(),
                user1.getPassword(),
                user1.getUserType(),
                user1.getProfile().getUserName(),
                null,
                null)))
                        .isFalse();
        assertThat(user1.isEqual(testCreater.newInstance(
                user1.getId(),
                user1.getLoginId(),
                user1.getPassword(),
                user1.getUserType(),
                user1.getProfile().getUserName(),
                user1.getProfile().getPhoneNumber(),
                null)))
                        .isFalse();
        assertThat(user1.isEqual(testCreater.newInstance(
                user1.getId(),
                user1.getLoginId(),
                user1.getPassword(),
                user1.getUserType(),
                user1.getProfile().getUserName(),
                user1.getProfile().getPhoneNumber(),
                user1.getProfile().getContact())))
                        .isTrue();
    }
}
