package io.extact.msa.spring.rms.infrastructure.persistence.remote;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

import io.extact.msa.spring.rms.domain.user.model.User;
import io.extact.msa.spring.rms.domain.user.model.User.UserCreatable;
import io.extact.msa.spring.rms.domain.user.model.UserId;
import io.extact.msa.spring.rms.domain.user.model.UserType;
import io.extact.msa.spring.rms.infrastructure.persistence.remote.user.RemoteUser;

class RemoteUserTest {

    private static final UserCreatable testCreator = new UserCreatable() {};

    @Test
    void testConstructor() {
        // given
        Integer id = 1;
        String loginId = "john.doe";
        String password = "password123";
        String userName = "John Doe";
        String phoneNumber = "123-456-7890";
        String contact = "john.doe@example.com";
        UserType userType = UserType.ADMIN;

        // when
        RemoteUser remoteUser = new RemoteUser(id, loginId, password, userName, phoneNumber, contact, userType);

        // then
        assertThat(remoteUser).isNotNull();
        assertThat(remoteUser.id()).isEqualTo(id);
        assertThat(remoteUser.loginId()).isEqualTo(loginId);
        assertThat(remoteUser.password()).isEqualTo(password);
        assertThat(remoteUser.userName()).isEqualTo(userName);
        assertThat(remoteUser.phoneNumber()).isEqualTo(phoneNumber);
        assertThat(remoteUser.contact()).isEqualTo(contact);
        assertThat(remoteUser.userType()).isEqualTo(userType);
    }

    @Test
    void testFromUser() {
        // given
        User user = testCreator.newInstance(
                new UserId(1),
                "jane.doe",
                "securePass123",
                UserType.MEMBER,
                "Jane Doe",
                "098-765-4321",
                "jane.doe@example.com");

        // when
        RemoteUser remoteUser = RemoteUser.from(user);

        // then
        assertThat(remoteUser).isNotNull();
        assertThat(remoteUser.id()).isEqualTo(user.getId().id());
        assertThat(remoteUser.loginId()).isEqualTo(user.getLoginId());
        assertThat(remoteUser.password()).isEqualTo(user.getPassword());
        assertThat(remoteUser.userName()).isEqualTo(user.getProfile().getUserName());
        assertThat(remoteUser.phoneNumber()).isEqualTo(user.getProfile().getPhoneNumber());
        assertThat(remoteUser.contact()).isEqualTo(user.getProfile().getContact());
        assertThat(remoteUser.userType()).isEqualTo(user.getUserType());
    }

    @Test
    void testToModel() {
        // given
        RemoteUser remoteUser = new RemoteUser(
                1,
                "john.doe",
                "password123",
                "John Doe",
                "123-456-7890",
                "john.doe@example.com",
                UserType.ADMIN);

        // when
        User user = remoteUser.toModel(null);

        // then
        assertThat(user).isNotNull();
        assertThat(user.getId().id()).isEqualTo(1);
        assertThat(user.getLoginId()).isEqualTo("john.doe");
        assertThat(user.getPassword()).isEqualTo("password123");
        assertThat(user.getProfile().getUserName()).isEqualTo("John Doe");
        assertThat(user.getProfile().getPhoneNumber()).isEqualTo("123-456-7890");
        assertThat(user.getProfile().getContact()).isEqualTo("john.doe@example.com");
        assertThat(user.getUserType()).isEqualTo(UserType.ADMIN);
    }
}
