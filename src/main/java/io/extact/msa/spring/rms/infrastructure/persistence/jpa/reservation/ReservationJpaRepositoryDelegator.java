package io.extact.msa.spring.rms.infrastructure.persistence.jpa.reservation;

import java.util.List;

import io.extact.msa.spring.platform.fw.infrastructure.persistence.jpa.JpaRepositoryDelegator;

public interface ReservationJpaRepositoryDelegator extends JpaRepositoryDelegator<ReservationEntity> {

    List<ReservationEntity> findByReserverIdOrderByIdAsc(int id);

    List<ReservationEntity> findByItemIdOrderByIdAsc(int id);
}
