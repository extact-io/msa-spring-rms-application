package io.extact.msa.spring.rms.domain.reservation;

import java.time.LocalDate;
import java.util.List;

import io.extact.msa.spring.platform.fw.domain.repository.GenericRepository;
import io.extact.msa.spring.platform.fw.domain.type.DateTimePeriod;
import io.extact.msa.spring.rms.domain.item.model.ItemId;
import io.extact.msa.spring.rms.domain.reservation.model.Reservation;
import io.extact.msa.spring.rms.domain.user.model.UserId;

public interface ReservationRepository extends GenericRepository<Reservation> {

    /**
     * レンタル品IDと利用開始日が一致する予約一覧を取得する。
     *
     * @param rentalItemId レンタル品ID
     * @param from 利用開始日
     * @return 該当予約。該当がない場合は空リスト
     */
    List<Reservation> findByItemIdAndFromDate(ItemId itemId, LocalDate from);

    /**
     * 指定されたユーザIDが予約者の予約一覧を取得する。
     * @param reserverId 予約者のユーザID
     * @return 該当予約。該当がない場合は空リスト
     */
    List<Reservation> findByReserverId(UserId reserverId);

    /**
     * 指定されたレンタル品の予約一覧を取得する。
     * @param rentalItemId レンタル品ID
     * @return 該当予約。該当がない場合は空リスト
     */
    List<Reservation> findByRentalItemId(ItemId itemId);

    /**
     * レンタルの予定期間が重なっている予約を取得する
     *
     * @param period レンタル予定期間
     * @return 予約。該当がない場合は空リスト
     */
    default List<Reservation> findOverlappingReservations(DateTimePeriod period) {
        return findAll()
                .stream()
                .filter(r -> r.isOverlappedBy(period))
                .toList();
    }

    /**
     * 指定されたレンタル品のレンタル予定期間が重なっている予約を取得する
     *
     * @param itemId レンタル品
     * @param period レンタル予定期間
     * @return 予約。該当がない場合は空リスト
     */
    default List<Reservation> findOverlappingReservations(ItemId itemId, DateTimePeriod period) {
        return findByRentalItemId(itemId)
                .stream()
                .filter(r -> r.isOverlappedBy(period))
                .toList();
    }
}