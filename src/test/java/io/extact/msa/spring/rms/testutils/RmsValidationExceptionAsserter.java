package io.extact.msa.spring.rms.testutils;

import static org.assertj.core.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import io.extact.msa.spring.platform.fw.exception.RmsValidationException;
import io.extact.msa.spring.platform.fw.exception.message.ValidationErrorMessage;
import io.extact.msa.spring.platform.fw.exception.message.ValidationErrorMessage.MessageItem;
import io.extact.msa.spring.platform.fw.feature.validator.SpringModelValidatorAdapter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RmsValidationExceptionAsserter {

    static String VALIDATION_ERROR_MESSAGE = "パラメーターエラーが発生しました";

    private final RmsValidationException thrown;

    public static RmsValidationExceptionAsserter asserterTo(RmsValidationException thrown) {
        return new RmsValidationExceptionAsserter(thrown);
    }

    public RmsValidationExceptionAsserter verifyMessageHeader() {
        assertThat(thrown).hasMessageContaining(VALIDATION_ERROR_MESSAGE);

        ValidationErrorMessage message = thrown.getErrorMessage();
        assertThat(message.errorReason()).isEqualTo(SpringModelValidatorAdapter.class.getSimpleName());
        assertThat(message.errorMessage()).isEqualTo(VALIDATION_ERROR_MESSAGE);

        return this;
    }

    public RmsValidationExceptionAsserter verifyErrorItemOf(String fieldName, String errorMessage) {
        ValidationErrorMessage message = thrown.getErrorMessage();
        List<MessageItem> items = message.messageItems();
        assertThat(items)
                .containsExactlyInAnyOrderElementsOf(
                        List.of(new MessageItem(fieldName, errorMessage)));
        return this;
    }

    public RmsValidationExceptionAsserter verifyErrorItemOf(Map<String, String> expectedMap) {

        List<MessageItem> expectItems = expectedMap.entrySet().stream()
                .map(entry -> new MessageItem(entry.getKey(), entry.getValue()))
                .toList();

        ValidationErrorMessage message = thrown.getErrorMessage();
        List<MessageItem> items = message.messageItems();

        assertThat(items).containsExactlyInAnyOrderElementsOf(expectItems);

        return this;
    }

    public RmsValidationExceptionAsserter verifyErrorItemFieldOf(String... fields) {

        ValidationErrorMessage message = thrown.getErrorMessage();
        List<String> itemFields = message.messageItems()
                .stream()
                .map(MessageItem::fieldName)
                .toList();

        assertThat(itemFields).containsExactlyInAnyOrderElementsOf(Arrays.asList(fields));

        return this;
    }
}