package io.extact.msa.spring.rms.application.admin;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

import io.extact.msa.spring.rms.domain.user.model.UserType;

class UserAddCommandTest {

    @Test
    void testBuilder() {
        // given
        String loginId = "testLogin";
        String password = "testPass";
        UserType userType = UserType.MEMBER;
        String userName = "Test User";
        String phoneNumber = "090-1234-5678";
        String contact = "test@example.com";

        // when
        UserAddCommand command = UserAddCommand.builder()
                .loginId(loginId)
                .password(password)
                .userType(userType)
                .userName(userName)
                .phoneNumber(phoneNumber)
                .contact(contact)
                .build();

        // then
        assertThat(command.loginId()).isEqualTo(loginId);
        assertThat(command.password()).isEqualTo(password);
        assertThat(command.userType()).isEqualTo(userType);
        assertThat(command.userName()).isEqualTo(userName);
        assertThat(command.phoneNumber()).isEqualTo(phoneNumber);
        assertThat(command.contact()).isEqualTo(contact);
    }
}