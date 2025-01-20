package io.extact.msa.spring.rms.application.admin;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

import io.extact.msa.spring.rms.domain.user.model.UserId;
import io.extact.msa.spring.rms.domain.user.model.UserType;

class UserUpdateCommandTest {

    @Test
    void testBuilder() {
        // given
        UserId userId = new UserId(1);
        String password = "newPass";
        UserType userType = UserType.ADMIN;
        String userName = "Updated User";
        String phoneNumber = "080-9876-5432";
        String contact = "update@example.com";

        // when
        UserUpdateCommand command = UserUpdateCommand.builder()
                .id(userId)
                .password(password)
                .userType(userType)
                .userName(userName)
                .phoneNumber(phoneNumber)
                .contact(contact)
                .build();

        // then
        assertThat(command.id()).isEqualTo(userId);
        assertThat(command.password()).isEqualTo(password);
        assertThat(command.userType()).isEqualTo(userType);
        assertThat(command.userName()).isEqualTo(userName);
        assertThat(command.phoneNumber()).isEqualTo(phoneNumber);
        assertThat(command.contact()).isEqualTo(contact);
    }
}
