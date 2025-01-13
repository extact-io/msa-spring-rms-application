package io.extact.msa.spring.rms;

import static org.assertj.core.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import io.extact.msa.spring.platform.fw.exception.RmsValidationException;
import io.extact.msa.spring.platform.fw.exception.response.ValidationErrorItem;
import io.extact.msa.spring.platform.fw.exception.response.ValidationErrorMessage;
import io.extact.msa.spring.platform.fw.infrastructure.framework.validator.SpringModelValidatorAdapter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RmsValidationExceptionAsserter {

    static String VALIDATION_ERROR_MESSAGE = "バリデーションエラーが発生しました";

    private final RmsValidationException e;

    public static RmsValidationExceptionAsserter asserterTo(RmsValidationException e) {
        return new RmsValidationExceptionAsserter(e);
    }

    public RmsValidationExceptionAsserter verifyMessageHeader() {
        assertThat(e).hasMessageContaining(VALIDATION_ERROR_MESSAGE);

        ValidationErrorMessage message = e.getErrorMessage();
        assertThat(message.errorReason()).isEqualTo(SpringModelValidatorAdapter.class.getSimpleName());
        assertThat(message.errorMessage()).isEqualTo(VALIDATION_ERROR_MESSAGE);

        return this;
    }

    public RmsValidationExceptionAsserter verifyErrorItemOf(String fieldName, String errorMessage) {
        ValidationErrorMessage message = e.getErrorMessage();
        List<ValidationErrorItem> items = message.validationErrorItems();
        assertThat(items)
                .containsExactlyInAnyOrderElementsOf(
                        List.of(new ValidationErrorItem(fieldName, errorMessage)));
        return this;
    }

    public RmsValidationExceptionAsserter verifyErrorItemOf(Map<String, String> expectedMap) {

        List<ValidationErrorItem> expectItems = expectedMap.entrySet().stream()
                .map(entry -> new ValidationErrorItem(entry.getKey(), entry.getValue()))
                .toList();

        ValidationErrorMessage message = e.getErrorMessage();
        List<ValidationErrorItem> items = message.validationErrorItems();

        assertThat(items).containsExactlyInAnyOrderElementsOf(expectItems);

        return this;
    }

    public RmsValidationExceptionAsserter verifyErrorItemFieldOf(String... fields) {

        ValidationErrorMessage message = e.getErrorMessage();
        List<String> itemFields = message.validationErrorItems()
                .stream()
                .map(ValidationErrorItem::fieldName)
                .toList();

        assertThat(itemFields).containsExactlyInAnyOrderElementsOf(Arrays.asList(fields));

        return this;
    }
}