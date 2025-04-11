package io.extact.msa.spring.rms.infrastructure.persistence.remote.stub;

import static io.extact.msa.spring.rms.PersistedTestData.*;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.annotation.PostConstruct;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.extact.msa.spring.platform.fw.interfaces.webapi.RmsRestController;
import io.extact.msa.spring.rms.domain.reservation.model.Reservation;
import io.extact.msa.spring.rms.infrastructure.persistence.remote.reservation.RemoteReservation;

@RmsRestController("/reservations")
public class RemoteReservationStubController extends RemoteStubController<Reservation, RemoteReservation> {

    private Map<Integer, RemoteReservation> reservationsMap;

    @PostConstruct
    void init() {
        reservationsMap = new LinkedHashMap<>();
        reservationsMap.put(reservation1.getId().id(), RemoteReservation.from(reservation1));
        reservationsMap.put(reservation2.getId().id(), RemoteReservation.from(reservation2));
        reservationsMap.put(reservation3.getId().id(), RemoteReservation.from(reservation3));
    }
    
    @GetMapping("/cond")
    public List<RemoteReservation> findByCondition(
            @RequestParam(name = "item-id", required = false) Integer itemId,
            @RequestParam(name = "reserver-id", required = false) Integer reserverId,
            @RequestParam(name = "from-date", required = false) LocalDate from) {
        
        return reservationsMap.values().stream()
                .filter(r -> itemId == null || itemId.equals(r.itemId()))
                .filter(r -> reserverId == null || reserverId.equals(r.reserverId()))
                .filter(r -> from == null || from.equals(r.fromDateTime().toLocalDate()))
                .toList();
    }
    
    @Override
    public Map<Integer, RemoteReservation> entityMap() {
        return reservationsMap;
    }
}
