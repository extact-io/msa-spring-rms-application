package io.extact.msa.spring.rms.infrastructure.persistence.jpa.reservation;

import java.time.LocalDateTime;
import java.util.List;

import io.extact.msa.spring.platform.fw.infrastructure.persistence.jpa.JpaRepositoryDelegator;

public interface ReservationJpaRepositoryDelegator extends JpaRepositoryDelegator<ReservationEntity> {

    List<ReservationEntity> findByItemIdAndFromDateTimeBetween(
            int itemId,
            LocalDateTime startDate,
            LocalDateTime endDate);

    List<ReservationEntity> findByReserverId(int id);

    List<ReservationEntity> findByItemId(int id);
}
