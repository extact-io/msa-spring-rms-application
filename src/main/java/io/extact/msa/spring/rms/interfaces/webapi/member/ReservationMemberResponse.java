package io.extact.msa.spring.rms.interfaces.webapi.member;

import java.time.LocalDateTime;

import io.extact.msa.spring.rms.application.support.ReservationComposeModel;

/**
 * 会員機能向けの予約レスポンス。
 * 会員機能では自分の予約しか扱わないため、ユーザの詳細情報は不要となる。
 * よって、会員機能とは異なり必要なデータだけをフラットな構造で持つようにしている。
 */
public record ReservationMemberResponse(
        int id,
        LocalDateTime fromDateTime,
        LocalDateTime toDateTime,
        String note,
        int itemId,
        String serialNo,
        String itemName,
        int reserverId) {

    static ReservationMemberResponse from(ReservationComposeModel model) {
        if (model == null) {
            return null;
        }
        return new ReservationMemberResponse(
                model.reservation().getId().id(),
                model.reservation().getFromDateTime(),
                model.reservation().getToDateTime(),
                model.reservation().getNote(),
                model.rentalItem().getId().id(),
                model.rentalItem().getSerialNo(),
                model.rentalItem().getItemName(),
                model.reserver().getId().id());
    }
}
