package io.extact.msa.spring.rms.interfaces.webapi.member;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import io.extact.msa.spring.platform.fw.web.RmsRestController;
import io.extact.msa.spring.rms.application.member.ReservationMemberService;
import io.extact.msa.spring.rms.domain.item.model.ItemId;
import io.extact.msa.spring.rms.domain.reservation.model.ReservationId;
import io.extact.msa.spring.rms.domain.user.model.UserId;
import lombok.RequiredArgsConstructor;

@RmsRestController("/reservations")
@RequiredArgsConstructor
public class ReservationMemberController {

    private final ReservationMemberService service;

    public List<ItemMemberResponse> getItemAll() {
        return service
                .getItemAll()
                .stream()
                .map(ItemMemberResponse::from)
                .toList();
    }

    public List<ItemMemberResponse> findCanRentedItemAtPeriod(LocalDateTime from, LocalDateTime to) {

        return service
                .findCanRentedItemAtPeriod(from, to)
                .stream()
                .map(ItemMemberResponse::from)
                .toList();
    }

    public boolean canRentedItemAtPeriod(int itemId, LocalDateTime from, LocalDateTime to) {
        return service
                .canRentedItemAtPeriod(new ItemId(itemId), from, to);
    }

    public List<ReservationMemberResponse> findReservationByItemId(int itemId) {
        return service
                .findReservationByItemId(new ItemId(itemId))
                .stream()
                .map(ReservationMemberResponse::from)
                .toList();
    }

    public List<ReservationMemberResponse> findReservationByItemIdAndFromDate(int itemId, LocalDate from) {
        return service
                .findReservationByItemIdAndFromDate(new ItemId(itemId), from)
                .stream()
                .map(ReservationMemberResponse::from)
                .toList();
    }

    public List<ReservationMemberResponse> findReservationByReserverId(int reserverId) {
        return service
                .findReservationByReserverId(new UserId(reserverId))
                .stream()
                .map(ReservationMemberResponse::from)
                .toList();
    }

    public List<ReservationMemberResponse> getOwnReservations() {
        return service
                .getOwnReservations()
                .stream()
                .map(ReservationMemberResponse::from)
                .toList();
    }

    public ReservationMemberResponse reserve(ReserveItemRequest request) {
        return service
                .reserve(request.toCommand())
                .transform(ReservationMemberResponse::from);
    }

    public void cancel(int reservationId) {
        service.cancel(new ReservationId(reservationId));
    }
}
