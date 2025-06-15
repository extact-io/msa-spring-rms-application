package io.extact.msa.spring.rms.webapi.member;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import io.extact.msa.spring.platform.fw.domain.constraint.RmsId;
import io.extact.msa.spring.platform.fw.feature.exception.RmsRequestCheckException;
import io.extact.msa.spring.platform.fw.interfaces.webapi.RmsRestController;
import io.extact.msa.spring.rms.application.member.ReserveItemQueryCondition;
import io.extact.msa.spring.rms.application.member.ReserveItemService;
import io.extact.msa.spring.rms.domain.item.model.ItemId;
import io.extact.msa.spring.rms.domain.reservation.model.ReservationId;
import lombok.RequiredArgsConstructor;

@RmsRestController
@RequiredArgsConstructor
public class ReserveItemController {

    private final ReserveItemService service;

    @GetMapping("/items")
    public List<ItemResponse> getItemAll() {
        return service
                .getItemAll()
                .stream()
                .map(ItemResponse::from)
                .toList();
    }

    @GetMapping("/items/rentable")
    public List<ItemResponse> findRentableItemAtPeriod(
            @RequestParam LocalDateTime from,
            @RequestParam LocalDateTime to) {

        return service
                .findRentableItemAtPeriod(from, to)
                .stream()
                .map(ItemResponse::from)
                .toList();
    }

    @GetMapping("/items/{itemId}/rentable")
    public boolean isRentableItemAtPeriod(
            @PathVariable @RmsId Integer itemId,
            @RequestParam LocalDateTime from,
            @RequestParam LocalDateTime to) {

        return service
                .isRentableItemAtPeriod(new ItemId(itemId), from, to);
    }

    // new
    @GetMapping("/reservations")
    public List<ReserveItemResponse> findReservationByCondition(
            @RequestParam(name = "item-id", required = false) Integer itemId,
            @RequestParam(name = "reserver-id", required = false) Integer reserverId,
            @RequestParam(name = "from-date", required = false) LocalDate from) {

        ReserveItemQueryCondition cond = ReserveItemQueryCondition.builder()
                .itemId(itemId)
                .reserverId(reserverId)
                .from(from)
                .build();

        if (!cond.hasAnyCondition()) {
            throw new RmsRequestCheckException("search requires at least one request parameter.");
        }

        return service
                .findReservationByCondition(cond)
                .stream()
                .map(ReserveItemResponse::from)
                .toList();
    }

    @GetMapping("/reservations/own")
    public List<ReserveItemResponse> getOwnReservations() {
        return service
                .getOwnReservations()
                .stream()
                .map(ReserveItemResponse::from)
                .toList();
    }

    @PostMapping("/reservations")
    public ReserveItemResponse reserve(@Valid @RequestBody ReserveItemRequest request) {
        return service
                .reserve(request.toCommand())
                .transform(ReserveItemResponse::from);
    }

    @DeleteMapping("/reservations/{reservationId}")
    public void cancel(
            @PathVariable @RmsId Integer reservationId) {
        service.cancel(new ReservationId(reservationId));
    }
}
