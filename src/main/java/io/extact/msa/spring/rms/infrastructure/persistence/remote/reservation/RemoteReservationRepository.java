package io.extact.msa.spring.rms.infrastructure.persistence.remote.reservation;

import java.util.List;
import java.util.Optional;

import io.extact.msa.spring.platform.fw.domain.model.Identity;
import io.extact.msa.spring.rms.application.member.ReserveItemQueryCondition;
import io.extact.msa.spring.rms.application.member.ReserveItemQueryService;
import io.extact.msa.spring.rms.domain.item.model.ItemId;
import io.extact.msa.spring.rms.domain.reservation.ReservationRepository;
import io.extact.msa.spring.rms.domain.reservation.model.Reservation;
import io.extact.msa.spring.rms.domain.reservation.model.ReservationModelView;
import io.extact.msa.spring.rms.domain.user.model.UserId;

public class RemoteReservationRepository implements ReservationRepository, ReserveItemQueryService {

    @Override
    public Optional<Reservation> find(Identity id) {
        return Optional.empty();
    }

    @Override
    public List<Reservation> findAll() {
        return null;
    }

    @Override
    public void add(Reservation model) {
    }

    @Override
    public void update(Reservation model) {
    }

    @Override
    public void delete(Reservation model) {
    }

    @Override
    public int nextIdentity() {
        return 0;
    }

    @Override
    public List<Reservation> findByReserverId(UserId reserverId) {
        return null;
    }

    @Override
    public List<Reservation> findByItemId(ItemId itemId) {
        return null;
    }

    @Override
    public List<ReservationModelView> findByCondition(ReserveItemQueryCondition cond) {
        return null;
    }
}
