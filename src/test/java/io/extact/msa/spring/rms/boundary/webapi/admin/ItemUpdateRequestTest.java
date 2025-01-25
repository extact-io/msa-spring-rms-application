package io.extact.msa.spring.rms.boundary.webapi.admin;

import static org.assertj.core.api.Assertions.*;

import jakarta.validation.Validator;
import jakarta.validation.metadata.BeanDescriptor;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import io.extact.msa.spring.platform.fw.domain.constraint.RmsId;
import io.extact.msa.spring.platform.fw.domain.constraint.ValidationConfig;
import io.extact.msa.spring.rms.ConstraintAnnotationAsserter;
import io.extact.msa.spring.rms.domain.item.constraint.ItemName;
import io.extact.msa.spring.rms.domain.item.constraint.SerialNo;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class ItemUpdateRequestTest {

    @Autowired
    private Validator validator;

    @Configuration(proxyBeanMethods = false)
    @Import(ValidationConfig.class)
    static class TestConfig {
    }

    @Test
    void testBuilder() {
        // given
        Integer id = 123;
        String serialNo = "serialNo";
        String itemName = "itemName";

        // when
        ItemUpdateRequest request = ItemUpdateRequest.builder()
                .id(id)
                .serialNo(serialNo)
                .itemName(itemName)
                .build();

        // then
        assertThat(request.id()).isEqualTo(id);
        assertThat(request.serialNo()).isEqualTo(serialNo);
        assertThat(request.itemName()).isEqualTo(itemName);
    }

    @Test
    void testApplyAnnotationCorrectly() {

        BeanDescriptor bd = validator.getConstraintsForClass(ItemUpdateRequest.class);
        ConstraintAnnotationAsserter.asserterTo(bd)
                .verifyPropertyAnnotations("id", RmsId.class)
                .verifyPropertyAnnotations("serialNo", SerialNo.class)
                .verifyPropertyAnnotations("itemName", ItemName.class);
    }
}
