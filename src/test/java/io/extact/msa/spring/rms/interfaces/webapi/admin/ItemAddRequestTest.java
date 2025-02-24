package io.extact.msa.spring.rms.interfaces.webapi.admin;

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

import io.extact.msa.spring.platform.fw.infrastructure.framework.validator.ValidatorConfig;
import io.extact.msa.spring.rms.domain.item.constraint.ItemName;
import io.extact.msa.spring.rms.domain.item.constraint.SerialNo;
import io.extact.msa.spring.rms.testutils.ConstraintAnnotationAsserter;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@ActiveProfiles("test")
class ItemAddRequestTest {

    @Autowired
    private Validator validator;

    @Configuration(proxyBeanMethods = false)
    @Import(ValidatorConfig.class)
    static class TestConfig {
    }

    @Test
    void testBuilder() {
        // given
        String serialNo = "serialNo";
        String itemName = "itemName";

        // when
        ItemAddRequest request = ItemAddRequest.builder()
                .serialNo(serialNo)
                .itemName(itemName)
                .build();

        // then
        assertThat(request.serialNo()).isEqualTo(serialNo);
        assertThat(request.itemName()).isEqualTo(itemName);
    }

    @Test
    void testApplyAnnotationCorrectly() {

        BeanDescriptor bd = validator.getConstraintsForClass(ItemAddRequest.class);
        ConstraintAnnotationAsserter.asserterTo(bd)
                .verifyPropertyAnnotations("serialNo", SerialNo.class)
                .verifyPropertyAnnotations("itemName", ItemName.class);
    }
}
