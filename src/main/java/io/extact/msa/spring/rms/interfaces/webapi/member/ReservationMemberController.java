package io.extact.msa.spring.rms.interfaces.webapi.member;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.websocket.server.PathParam;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.extact.msa.spring.platform.fw.domain.constraint.RmsId;
import io.extact.msa.spring.platform.fw.web.RmsRestController;
import io.extact.msa.spring.rms.application.member.ReservationMemberService;
import io.extact.msa.spring.rms.domain.item.model.ItemId;
import io.extact.msa.spring.rms.domain.reservation.model.ReservationId;
import io.extact.msa.spring.rms.domain.user.model.UserId;
import lombok.RequiredArgsConstructor;

@RmsRestController("/rental-items")
@RequiredArgsConstructor
public class ReservationMemberController {

    private final ReservationMemberService service;

    @GetMapping
    public List<ItemMemberResponse> getItemAll() {
        return service
                .getItemAll()
                .stream()
                .map(ItemMemberResponse::from)
                .toList();
    }

    @GetMapping("/rentable")
    public List<ItemMemberResponse> findCanRentedItemAtPeriod(
            @RequestParam("from") @NotNull LocalDateTime from,
            @RequestParam("to") @NotNull LocalDateTime to) {

        return service
                .findCanRentedItemAtPeriod(from, to)
                .stream()
                .map(ItemMemberResponse::from)
                .toList();
    }

    @GetMapping("/{itemId}/rentable")
    public boolean canRentedItemAtPeriod(
            @PathParam("itemId") @RmsId int itemId,
            @RequestParam("from") @NotNull LocalDateTime from,
            @RequestParam("to") @NotNull LocalDateTime to) {

        return service
                .canRentedItemAtPeriod(new ItemId(itemId), from, to);
    }

    @GetMapping("/{itemId}/reservations")
    public List<ReservationMemberResponse> findReservationByItemId(
            @PathParam("itemId") @RmsId int itemId) {

        return service
                .findReservationByItemId(new ItemId(itemId))
                .stream()
                .map(ReservationMemberResponse::from)
                .toList();
    }

    @GetMapping("/{itemId}/reservations/")
    public List<ReservationMemberResponse> findReservationByItemIdAndFromDate(
            @PathParam("itemId") @RmsId int itemId,
            @RequestParam("from") @NotNull LocalDate from) {

        return service
                .findReservationByItemIdAndFromDate(new ItemId(itemId), from)
                .stream()
                .map(ReservationMemberResponse::from)
                .toList();
    }

    @GetMapping("/reservations/reserver/{reserverId}")
    public List<ReservationMemberResponse> findReservationByReserverId(
            @PathParam("reserverId") @RmsId int reserverId) {

        return service
                .findReservationByReserverId(new UserId(reserverId))
                .stream()
                .map(ReservationMemberResponse::from)
                .toList();
    }

    @GetMapping("/reservations/own")
    public List<ReservationMemberResponse> getOwnReservations() {
        return service
                .getOwnReservations()
                .stream()
                .map(ReservationMemberResponse::from)
                .toList();
    }

    @PostMapping("/reservations")
    public ReservationMemberResponse reserve(@Valid ReserveItemRequest request) {
        return service
                .reserve(request.toCommand())
                .transform(ReservationMemberResponse::from);
    }

    @DeleteMapping("/reservations/{reservationId}")
    public void cancel(
            @PathParam("reservationId") @RmsId int reservationId) {
        service.cancel(new ReservationId(reservationId));
    }
}
