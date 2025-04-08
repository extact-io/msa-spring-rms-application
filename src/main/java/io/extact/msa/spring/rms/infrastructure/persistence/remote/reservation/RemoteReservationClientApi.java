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

import io.extact.msa.spring.platform.fw.infrastructure.persistence.remote.GenericClientApi;

@HttpExchange("/reservations")
// TODO:パスに個別の値はでてこないので、GenericClientApiに@GetExchangeを定義してスッキリさせる作戦ができるかも
public interface RemoteReservationClientApi extends GenericClientApi<RemoteReservation> {

    @GetExchange("/{id}")
    @Override
    RemoteReservation get(@PathVariable Integer id);

    @PostExchange
    @Override
    void add(@RequestBody RemoteReservation reservation);

    @PutExchange
    @Override
    boolean update(@RequestBody RemoteReservation reservation);

    @DeleteExchange("/{id}")
    @Override
    boolean delete(@PathVariable Integer id);

    @GetExchange("/next-identity")
    @Override
    int nextIdentity();
    
    @GetExchange
    public List<RemoteReservation> findByCondition(
            @RequestParam(name = "item-id", required = false) Integer itemId,
            @RequestParam(name = "reserver-id", required = false) Integer reserverId,
            @RequestParam(name = "from-date", required = false) LocalDate from);
}
