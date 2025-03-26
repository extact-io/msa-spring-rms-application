package io.extact.msa.spring.rms.application.admin.event;

import org.springframework.context.event.EventListener;

import io.extact.msa.spring.platform.fw.exception.BusinessFlowException;
import io.extact.msa.spring.platform.fw.exception.BusinessFlowException.CauseType;
import io.extact.msa.spring.rms.domain.reservation.ReservationRepository;
import lombok.RequiredArgsConstructor;

/**
 * 予約の依存オブジェクトから発行されるイベントのリスナー。
 */
@RequiredArgsConstructor
public class ReservationDependencyEventListener {

    private final ReservationRepository repository;

    /**
     * レンタル品が削除されるよイベントに対するハンドラー。
     * 削除されるレンタル品に対する予約がある場合は削除されないように
     * 例外を送出する。
     *
     * @param event 削除されるイベント
     */
    @EventListener
    void handle(ItemWillBeDeletedEvent event) {
        repository.findByItemId(event.id())
                .stream()
                .findAny()
                .ifPresent(item -> {
                    throw new BusinessFlowException(
                            "Cannot be deleted because it is referenced in the reservation. itemId=" + event.id(),
                            CauseType.REFERED);
                });
    }

    /**
     * ユーザが削除されるよイベントに対するハンドラー。
     * 削除されるユーザに対する予約がある場合は削除されないように
     * 例外を送出する。
     *
     * @param event 削除されるイベント
     */
    @EventListener
    void handle(UserWillBeDeletedEvent event) {
        repository.findByReserverId(event.id())
                .stream()
                .findAny()
                .ifPresent(user -> {
                    throw new BusinessFlowException(
                            "Cannot be deleted because it is referenced in the reservation. userId=" + event.id(),
                            CauseType.REFERED);
                });
    }
}
