package io.extact.msa.spring.rms.infrastructure.item.jpa;

import java.util.Optional;

import io.extact.msa.spring.platform.fw.infrastructure.persistence.jpa.JpaRepositoryDelegator;

public interface ItemJpaRepositoryDelegator extends JpaRepositoryDelegator<ItemEntity> {

    Optional<ItemEntity> findBySerialNo(String serialNo);
}
