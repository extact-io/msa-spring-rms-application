package io.extact.msa.spring.rms.infrastructure.persistence.jpa.reservation;

import java.time.LocalDateTime;
import java.util.List;

import io.extact.msa.spring.platform.fw.infrastructure.persistence.jpa.JpaRepositoryDelegator;

public interface ReservationJpaRepositoryDelegator extends JpaRepositoryDelegator<ReservationEntity> {

    List<ReservationEntity> findByItemIdAndFromDateTimeBetweenByOrderByIdAsc(
            int itemId,
            LocalDateTime startDate,
            LocalDateTime endDate);

    List<ReservationEntity> findByReserverIdByOrderByIdAsc(int id);

    List<ReservationEntity> findByItemIdByOrderByIdAsc(int id);
}
