package io.extact.msa.spring.rms.infrastructure.persistence.remote.reservation;

import java.time.LocalDate;
import java.util.List;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

import io.extact.msa.spring.rms.domain.item.model.ItemId;
import io.extact.msa.spring.rms.domain.reservation.model.Reservation;
import io.extact.msa.spring.rms.domain.user.model.UserId;

@HttpExchange("/reservations")
public interface RemoteReservationClientApi {

    @GetExchange("/{id}")
    RemoteReservation get(@PathVariable Integer id);

    @GetExchange
    List<RemoteReservation> getAll();

    @PostExchange
    void add(@RequestBody RemoteReservation reservation);

    @PutExchange
    void update(@RequestBody RemoteReservation reservation);

    @DeleteExchange("/{id}")
    void delete(@PathVariable Integer id);

    @GetExchange("/next-identity")
    int nextIdentity();


	List<Reservation> findByItemIdAndFromDate(ItemId itemId, LocalDate from);

	List<Reservation> findByReserverId(UserId reserverId);

	List<Reservation> findByItemId(ItemId itemId);
}
