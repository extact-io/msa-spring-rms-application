package io.extact.msa.spring.rms.infrastructure.user.jpa;

import java.util.Optional;

import io.extact.msa.spring.platform.fw.infrastructure.persistence.jpa.JpaRepositoryDelegator;

public interface UserJpaRepositoryDelegator extends JpaRepositoryDelegator<UserEntity> {

    Optional<UserEntity> findByLoginId(String loginId);
    Optional<UserEntity> findByLoginIdAndPasswod(String loginId, String password);
}
