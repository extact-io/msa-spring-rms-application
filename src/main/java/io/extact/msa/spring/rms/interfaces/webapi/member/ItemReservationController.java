package io.extact.msa.spring.rms.interfaces.webapi.member;

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
import io.extact.msa.spring.rms.application.member.ItemReservationService;
import io.extact.msa.spring.rms.application.member.ReservationQueryCondition;
import io.extact.msa.spring.rms.application.support.ReservationComposeModel;
import io.extact.msa.spring.rms.domain.item.model.ItemId;
import io.extact.msa.spring.rms.domain.reservation.model.ReservationId;
import io.extact.msa.spring.rms.domain.user.model.UserId;
import lombok.RequiredArgsConstructor;

@RmsRestController("/reserve")
@RequiredArgsConstructor
public class ItemReservationController {

    private final ItemReservationService service;

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

    	ReservationQueryCondition cond = ReservationQueryCondition.builder()
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
    
    @GetMapping("/reservations/items/{itemId}")
    public List<ReserveItemResponse> findReservationByItemId(
            @PathVariable @RmsId Integer itemId,
            @RequestParam(name = "from-date", required = false) LocalDate from) {

        List<ReservationComposeModel> models = from != null
                ? service.findReservationByItemIdAndFromDate(new ItemId(itemId), from)
                : service.findReservationByItemId(new ItemId(itemId));

        return models.stream()
                .map(ReserveItemResponse::from)
                .toList();
    }

    @GetMapping("/reservations/reservers/{reserverId}")
    public List<ReserveItemResponse> findReservationByReserverId(
            @PathVariable @RmsId Integer reserverId) {

        return service
                .findReservationByReserverId(new UserId(reserverId))
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
