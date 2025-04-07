package io.extact.msa.spring.rms.infrastructure.persistence.remote.reservation;

import java.util.List;

import io.extact.msa.spring.platform.fw.infrastructure.persistence.ModelEntityMapper;
import io.extact.msa.spring.platform.fw.infrastructure.persistence.remote.AbstractRemoteRepository;
import io.extact.msa.spring.rms.application.member.ReserveItemQueryCondition;
import io.extact.msa.spring.rms.application.member.ReserveItemQueryService;
import io.extact.msa.spring.rms.domain.item.model.ItemId;
import io.extact.msa.spring.rms.domain.reservation.ReservationRepository;
import io.extact.msa.spring.rms.domain.reservation.model.Reservation;
import io.extact.msa.spring.rms.domain.reservation.model.ReservationModelView;
import io.extact.msa.spring.rms.domain.user.model.UserId;

public class RemoteReservationRepository extends AbstractRemoteRepository<Reservation, RemoteReservation>
        implements ReservationRepository, ReserveItemQueryService {

    private final RemoteReservationClientApi clientApi;
    private final ModelEntityMapper<Reservation, RemoteReservation> entityMapper;

    public RemoteReservationRepository(
            RemoteReservationClientApi clientApi, 
            ModelEntityMapper<Reservation, RemoteReservation> entityMapper) {
        
        super(clientApi, entityMapper);
        this.clientApi = clientApi;
        this.entityMapper = entityMapper;
    }

    @Override
    public List<Reservation> findByReserverId(UserId reserverId) {
        return clientApi
                .findByCondition(null, reserverId.id(), null)
                .stream()
                .map(entityMapper::toModel)
                .toList();
    }

    @Override
    public List<Reservation> findByItemId(ItemId itemId) {
        return clientApi
                .findByCondition(itemId.id(), null, null)
                .stream()
                .map(entityMapper::toModel)
                .toList();
    }

    @Override
    public List<ReservationModelView> findByCondition(ReserveItemQueryCondition cond) {
        return clientApi
                .findByCondition(cond.itemId(), cond.reserverId(), cond.from())
                .stream()
                .map(ReservationModelViewRemoteAdapter::new)
                .map(r -> (ReservationModelView) r)
                .toList();
    }
}
