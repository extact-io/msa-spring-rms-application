package io.extact.msa.spring.rms.application.universal;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

class UserProfileUpdateCommandTest {

    @Test
    void testBuilder() {
        // given
        String password = "newPassword123";
        String userName = "Updated User Name";
        String phoneNumber = "090-1234-5678";
        String contact = "updated@example.com";

        // when
        UserProfileUpdateCommand command = UserProfileUpdateCommand.builder()
                .password(password)
                .userName(userName)
                .phoneNumber(phoneNumber)
                .contact(contact)
                .build();

        // then
        assertThat(command.password()).isEqualTo(password);
        assertThat(command.userName()).isEqualTo(userName);
        assertThat(command.phoneNumber()).isEqualTo(phoneNumber);
        assertThat(command.contact()).isEqualTo(contact);
    }
}
