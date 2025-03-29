package io.extact.msa.spring.rms.infrastructure.persistence.remote.reservation;

import java.util.List;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

@HttpExchange("/reservations")
public interface RemoteReservationClientApi {

    @GetExchange("/{id}")
    RemoteReservationResponse get(@PathVariable("id") Integer id);

    @GetExchange
    List<RemoteReservationResponse> getAll();

    @PostExchange
    RemoteReservationResponse add(@RequestBody AddRemoteReserveRequest req);

    @PutExchange
    RemoteReservationResponse update(@RequestBody UpdateRemoteReservationRequest req);

    @DeleteExchange("/{id}")
    RemoteReservationResponse delete(@PathVariable("id") Integer itemId);

    @GetExchange("/next-identity")
    int nextIdentity();

    
}
