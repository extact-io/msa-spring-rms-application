package io.extact.msa.spring.rms.infrastructure.persistence.jpa.user;

import java.util.Optional;

import io.extact.msa.spring.platform.fw.infrastructure.persistence.jpa.JpaRepositoryDelegator;

public interface UserJpaRepositoryDelegator extends JpaRepositoryDelegator<UserEntity> {

    Optional<UserEntity> findByLoginId(String loginId);
    Optional<UserEntity> findByLoginIdAndPassword(String loginId, String password);
}
