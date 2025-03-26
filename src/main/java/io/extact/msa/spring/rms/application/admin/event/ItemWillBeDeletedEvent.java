package io.extact.msa.spring.rms.application.admin.event;

import io.extact.msa.spring.platform.fw.application.event.ApplicationServiceEvent;
import io.extact.msa.spring.rms.domain.item.model.ItemId;

/**
 * 「これからレンタル品を削除するよ」のイベント。
 */
public record ItemWillBeDeletedEvent(ItemId id) implements ApplicationServiceEvent {
}
