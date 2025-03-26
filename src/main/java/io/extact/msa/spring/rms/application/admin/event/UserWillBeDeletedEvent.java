package io.extact.msa.spring.rms.application.admin.event;

import io.extact.msa.spring.platform.fw.application.event.ApplicationServiceEvent;
import io.extact.msa.spring.rms.domain.user.model.UserId;

/**
 * 「これからユーザを削除するよ」のイベント。
 */
public record UserWillBeDeletedEvent(UserId id) implements ApplicationServiceEvent {
}
