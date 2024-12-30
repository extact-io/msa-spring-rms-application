package io.extact.msa.spring.rms.infrastructure.persistence.jpa.user;

import java.util.Optional;

import io.extact.msa.spring.platform.fw.infrastructure.ModelEntityMapper;
import io.extact.msa.spring.platform.fw.infrastructure.persistence.jpa.AbstractJpaRepository;
import io.extact.msa.spring.rms.domain.user.UserRepository;
import io.extact.msa.spring.rms.domain.user.model.User;

public class UserJpaRepository extends AbstractJpaRepository<User, UserEntity> implements UserRepository {

    private final UserJpaRepositoryDelegator delegator;
    private final ModelEntityMapper<User, UserEntity> entityMapper;

    public UserJpaRepository(UserJpaRepositoryDelegator delegator,
            ModelEntityMapper<User, UserEntity> entityMapper) {
        super(delegator, entityMapper);
        this.delegator = delegator;
        this.entityMapper = entityMapper;
    }

    @Override
    public Optional<User> findDuplicationData(User checkModel) {
        return delegator.findByLoginId(checkModel.getLoginId())
                .map(entityMapper::toModel);
    }

    @Override
    public Optional<User> findByLoginIdAndPasswod(String loginId, String password) {
        return delegator.findByLoginIdAndPasswod(loginId, password)
                .map(entityMapper::toModel);
    }
}
