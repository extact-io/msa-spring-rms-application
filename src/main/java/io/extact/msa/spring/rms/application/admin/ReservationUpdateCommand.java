package io.extact.msa.spring.rms.application.admin;

import io.extact.msa.spring.rms.domain.reservation.model.ReservationId;
import io.extact.msa.spring.rms.domain.reservation.model.ReservationPeriod;
import lombok.Builder;

/**
 * 予約管理機能に対する更新コマンドクラス。
 * 予約管理機能でもレンタル品と予約者の紐づけの変更は不可なのでレンタル品IDと
 * 予約者IDはコマンドに含めていない
 */
@Builder
public record ReservationUpdateCommand(
        ReservationId id,
        ReservationPeriod period,
        String note) {
}
