package io.extact.msa.spring.rms.infrastructure.persistence.jpa.reservation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import io.extact.msa.spring.platform.fw.infrastructure.ModelEntityMapper;
import io.extact.msa.spring.platform.fw.infrastructure.persistence.jpa.AbstractJpaRepository;
import io.extact.msa.spring.rms.domain.item.model.ItemId;
import io.extact.msa.spring.rms.domain.reservation.ReservationRepository;
import io.extact.msa.spring.rms.domain.reservation.model.Reservation;
import io.extact.msa.spring.rms.domain.user.model.UserId;

public class ReservationJpaRepository extends AbstractJpaRepository<Reservation, ReservationEntity>
        implements ReservationRepository {

    private final ReservationJpaRepositoryDelegator delegator;

    public ReservationJpaRepository(ReservationJpaRepositoryDelegator delegator,
            ModelEntityMapper<Reservation, ReservationEntity> entityMapper) {
        super(delegator, entityMapper);
        this.delegator = delegator;
    }

    @Override
    public List<Reservation> findByItemIdAndFromDate(ItemId itemId, LocalDate from) {
        LocalDateTime startOfDay = from.atStartOfDay();
        LocalDateTime endOfDay = from.atTime(LocalTime.MAX);
        return delegator.findByItemIdAndFromDateTimeBetweenByOrderByIdAsc(itemId.id(), startOfDay, endOfDay)
                .stream()
                .map(ReservationEntity::toModel)
                .toList();
    }

    @Override
    public List<Reservation> findByReserverId(UserId reserverId) {
        return delegator.findByReserverIdByOrderByIdAsc(reserverId.id())
                .stream()
                .map(ReservationEntity::toModel)
                .toList();
    }

    @Override
    public List<Reservation> findByItemId(ItemId itemId) {
        return delegator.findByItemIdByOrderByIdAsc(itemId.id())
                .stream()
                .map(ReservationEntity::toModel)
                .toList();
    }
}
