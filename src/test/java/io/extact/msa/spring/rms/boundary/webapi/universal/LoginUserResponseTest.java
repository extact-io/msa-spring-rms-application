package io.extact.msa.spring.rms.boundary.webapi.universal;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

import io.extact.msa.spring.rms.domain.user.model.UserId;
import io.extact.msa.spring.rms.domain.user.model.UserProfile;
import io.extact.msa.spring.rms.domain.user.model.UserReference;
import io.extact.msa.spring.rms.domain.user.model.UserType;

class LoginUserResponseTest {

    @Test
    void testFrom() {
        // given
        UserReference user = new UserReference(
                new UserId(1),
                "testLoginId",
                "testPassword",
                UserType.ADMIN,
                new UserProfile("John Doe", "123-456-7890", "john.doe@example.com")
        );

        // when
        LoginUserResponse response = LoginUserResponse.from(user);

        // then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1);
        assertThat(response.loginId()).isEqualTo("testLoginId");
        assertThat(response.password()).isEqualTo("testPassword");
        assertThat(response.userType()).isEqualTo(UserType.ADMIN);
        assertThat(response.userName()).isEqualTo("John Doe");
        assertThat(response.phoneNumber()).isEqualTo("123-456-7890");
        assertThat(response.contact()).isEqualTo("john.doe@example.com");
    }

    @Test
    void testFromNull() {
        // given
        UserReference user = null;

        // when
        LoginUserResponse response = LoginUserResponse.from(user);

        // then
        assertThat(response).isNull();
    }
}
