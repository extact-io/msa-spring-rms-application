package io.extact.msa.spring.rms.domain.user.event;

import io.extact.msa.spring.platform.fw.domain.model.DomainEvent;
import io.extact.msa.spring.rms.domain.user.model.User;

/**
 * 「ユーザが登録されたよ」のドメインイベント。
 */
public record UserAddedEvent(User aadedUser) implements DomainEvent {
}
