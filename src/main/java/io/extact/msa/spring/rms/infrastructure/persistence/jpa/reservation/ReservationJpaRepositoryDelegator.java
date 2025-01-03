package io.extact.msa.spring.rms.infrastructure.persistence.jpa.reservation;

import java.time.LocalDateTime;
import java.util.List;

import io.extact.msa.spring.platform.fw.infrastructure.persistence.jpa.JpaRepositoryDelegator;

public interface ReservationJpaRepositoryDelegator extends JpaRepositoryDelegator<ReservationEntity> {

    List<ReservationEntity> findByItemIdAndFromDateTimeBetweenOrderByIdAsc(
            int itemId,
            LocalDateTime from,
            LocalDateTime to);

    List<ReservationEntity> findByReserverIdOrderByIdAsc(int id);

    List<ReservationEntity> findByItemIdOrderByIdAsc(int id);
}
