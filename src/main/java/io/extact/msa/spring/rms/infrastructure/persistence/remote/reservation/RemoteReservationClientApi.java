package io.extact.msa.spring.rms.infrastructure.persistence.remote.reservation;

import java.time.LocalDate;
import java.util.List;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

import io.extact.msa.spring.rms.interfaces.webapi.member.ReserveItemResponse;

@HttpExchange("/reservations")
public interface RemoteReservationClientApi {

    @GetExchange("/{id}")
    RemoteReservation get(@PathVariable Integer id);

    @GetExchange
    public List<ReserveItemResponse> findByCondition(
            @RequestParam(name = "item-id", required = false) Integer itemId,
            @RequestParam(name = "reserver-id", required = false) Integer reserverId,
            @RequestParam(name = "from-date", required = false) LocalDate from);

    @PostExchange
    void add(@RequestBody RemoteReservation reservation);

    @PutExchange
    void update(@RequestBody RemoteReservation reservation);

    @DeleteExchange("/{id}")
    void delete(@PathVariable Integer id);

    @GetExchange("/next-identity")
    int nextIdentity();
}
