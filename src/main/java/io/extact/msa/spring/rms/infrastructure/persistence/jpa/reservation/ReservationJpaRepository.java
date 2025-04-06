package io.extact.msa.spring.rms.infrastructure.persistence.jpa.reservation;

import static org.springframework.data.domain.Sort.*;

import java.util.List;

import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;

import io.extact.msa.spring.platform.fw.infrastructure.persistence.jpa.AbstractJpaRepository;
import io.extact.msa.spring.platform.fw.infrastructure.persistence.jpa.ModelEntityMapper;
import io.extact.msa.spring.rms.application.member.ReserveItemQueryCondition;
import io.extact.msa.spring.rms.application.member.ReserveItemQueryService;
import io.extact.msa.spring.rms.domain.item.model.ItemId;
import io.extact.msa.spring.rms.domain.reservation.ReservationRepository;
import io.extact.msa.spring.rms.domain.reservation.model.Reservation;
import io.extact.msa.spring.rms.domain.reservation.model.ReservationModelView;
import io.extact.msa.spring.rms.domain.user.model.UserId;

public class ReservationJpaRepository extends AbstractJpaRepository<Reservation, ReservationEntity>
        implements ReservationRepository, ReserveItemQueryService {

    private final ReservationJpaRepositoryDelegator delegator;
    private final ModelEntityMapper<Reservation, ReservationEntity> entityMapper;

    public ReservationJpaRepository(ReservationJpaRepositoryDelegator delegator,
            ModelEntityMapper<Reservation, ReservationEntity> entityMapper) {
        super(delegator, entityMapper);
        this.delegator = delegator;
        this.entityMapper = entityMapper;
    }

    @Override
    public List<Reservation> findByReserverId(UserId reserverId) {
        return delegator.findByReserverIdOrderByIdAsc(reserverId.id())
                .stream()
                .map(entityMapper::toModel)
                .toList();
    }

    @Override
    public List<Reservation> findByItemId(ItemId itemId) {
        return delegator.findByItemIdOrderByIdAsc(itemId.id())
                .stream()
                .map(entityMapper::toModel)
                .toList();
    }

    @Override
    public List<ReservationModelView> findByCondition(ReserveItemQueryCondition cond) {
        Specification<ReservationEntity> spec = ReservationSpecification.fromCondition(cond);
        return delegator.findAll(spec, by(Direction.ASC, "id"))
                .stream()
                .map(ReservationModelViewAdapter::new)
                .map(adapter -> (ReservationModelView) adapter)
                .toList();
    }
}
