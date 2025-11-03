package io.extact.msa.spring.rms.webapi.universal;

import static org.assertj.core.api.Assertions.*;

import jakarta.validation.Validator;
import jakarta.validation.metadata.BeanDescriptor;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import io.extact.msa.spring.platform.fw.feature.validator.ValidatorConfig;
import io.extact.msa.spring.rms.domain.user.constraint.Contact;
import io.extact.msa.spring.rms.domain.user.constraint.Passowrd;
import io.extact.msa.spring.rms.domain.user.constraint.PhoneNumber;
import io.extact.msa.spring.rms.domain.user.constraint.UserName;
import io.extact.msa.spring.test.junit5.ConstraintAnnotationAsserter;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@ActiveProfiles("test")
class UserProfileUpdateRequestTest {

    @Autowired
    private Validator validator;

    @Configuration(proxyBeanMethods = false)
    @Import(ValidatorConfig.class)
    static class TestConfig {
    }

    @Test
    void testBuilder() {
        // given
        String password = "securePassword123";
        String userName = "John Doe";
        String phoneNumber = "123-456-7890";
        String contact = "john.doe@example.com";

        // when
        UserProfileUpdateRequest request = UserProfileUpdateRequest.builder()
                .password(password)
                .userName(userName)
                .phoneNumber(phoneNumber)
                .contact(contact)
                .build();

        // then
        assertThat(request.password()).isEqualTo(password);
        assertThat(request.userName()).isEqualTo(userName);
        assertThat(request.phoneNumber()).isEqualTo(phoneNumber);
        assertThat(request.contact()).isEqualTo(contact);
    }

    @Test
    void testApplyAnnotationCorrectly() {
        // given
        BeanDescriptor descriptor = validator.getConstraintsForClass(UserProfileUpdateRequest.class);
        // then
        ConstraintAnnotationAsserter.asserterTo(descriptor)
                .verifyPropertyAnnotations("password", Passowrd.class)
                .verifyPropertyAnnotations("userName", UserName.class)
                .verifyPropertyAnnotations("phoneNumber", PhoneNumber.class)
                .verifyPropertyAnnotations("contact", Contact.class);
    }
}
