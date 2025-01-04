package io.extact.msa.spring.rms.infrastructure.persistence.jpa;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

import io.extact.msa.spring.rms.domain.user.model.User;
import io.extact.msa.spring.rms.domain.user.model.User.UserCreatable;
import io.extact.msa.spring.rms.domain.user.model.UserId;
import io.extact.msa.spring.rms.domain.user.model.UserType;
import io.extact.msa.spring.rms.infrastructure.persistence.jpa.user.UserEntity;

class UserEntityTest {

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
        UserEntity userEntity = new UserEntity(id, loginId, password, userName, phoneNumber, contact, userType);

        // then
        assertThat(userEntity).isNotNull();
        assertThat(userEntity.getId()).isEqualTo(id);
        assertThat(userEntity.getLoginId()).isEqualTo(loginId);
        assertThat(userEntity.getPassword()).isEqualTo(password);
        assertThat(userEntity.getUserName()).isEqualTo(userName);
        assertThat(userEntity.getPhoneNumber()).isEqualTo(phoneNumber);
        assertThat(userEntity.getContact()).isEqualTo(contact);
        assertThat(userEntity.getUserType()).isEqualTo(userType);
    }

    @Test
    void testSetters() {
        // given
        UserEntity userEntity = new UserEntity();
        Integer id = 1;
        String loginId = "jane.doe";
        String password = "securePass123";
        String userName = "Jane Doe";
        String phoneNumber = "098-765-4321";
        String contact = "jane.doe@example.com";
        UserType userType = UserType.MEMBER;

        // when
        userEntity.setId(id);
        userEntity.setLoginId(loginId);
        userEntity.setPassword(password);
        userEntity.setUserName(userName);
        userEntity.setPhoneNumber(phoneNumber);
        userEntity.setContact(contact);
        userEntity.setUserType(userType);

        // then
        assertThat(userEntity.getId()).isEqualTo(id);
        assertThat(userEntity.getLoginId()).isEqualTo(loginId);
        assertThat(userEntity.getPassword()).isEqualTo(password);
        assertThat(userEntity.getUserName()).isEqualTo(userName);
        assertThat(userEntity.getPhoneNumber()).isEqualTo(phoneNumber);
        assertThat(userEntity.getContact()).isEqualTo(contact);
        assertThat(userEntity.getUserType()).isEqualTo(userType);
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
        UserEntity userEntity = UserEntity.from(user);

        // then
        assertThat(userEntity).isNotNull();
        assertThat(userEntity.getId()).isEqualTo(user.getId().id());
        assertThat(userEntity.getLoginId()).isEqualTo(user.getLoginId());
        assertThat(userEntity.getPassword()).isEqualTo(user.getPassword());
        assertThat(userEntity.getUserName()).isEqualTo(user.getProfile().getUserName());
        assertThat(userEntity.getPhoneNumber()).isEqualTo(user.getProfile().getPhoneNumber());
        assertThat(userEntity.getContact()).isEqualTo(user.getProfile().getContact());
        assertThat(userEntity.getUserType()).isEqualTo(user.getUserType());
    }

    @Test
    void testToModel() {
        // given
        UserEntity userEntity = new UserEntity(
                1,
                "john.doe",
                "password123",
                "John Doe",
                "123-456-7890",
                "john.doe@example.com",
                UserType.ADMIN);

        // when
        User user = userEntity.toModel();

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
