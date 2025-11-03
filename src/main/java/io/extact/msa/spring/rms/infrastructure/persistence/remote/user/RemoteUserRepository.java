package io.extact.msa.spring.rms.infrastructure.persistence.remote.user;

import java.util.Optional;

import io.extact.msa.spring.platform.fw.infrastructure.persistence.ModelEntityMapper;
import io.extact.msa.spring.platform.fw.infrastructure.persistence.remote.AbstractRemoteRepository;
import io.extact.msa.spring.rms.domain.user.UserRepository;
import io.extact.msa.spring.rms.domain.user.model.User;
import io.extact.msa.spring.rms.domain.user.model.UserId;

public class RemoteUserRepository extends AbstractRemoteRepository<User, UserId, RemoteUser> implements UserRepository {

    private final RemoteUserClientApi clientApi;
    private final ModelEntityMapper<User, RemoteUser> entityMapper;

    public RemoteUserRepository(
            RemoteUserClientApi clientApi,
            ModelEntityMapper<User, RemoteUser> entityMapper) {

        super(clientApi, entityMapper, UserId::new);
        this.clientApi = clientApi;
        this.entityMapper = entityMapper;
    }

    public Optional<User> findDuplicationData(User checkModel) {
        RemoteUser found = clientApi.findByLoginId(checkModel.getLoginId());
        return Optional
                .ofNullable(found)
                .map(entityMapper::toModel);
    }

    @Override
    public Optional<User> findByLoginIdAndPassword(String loginId, String password) {
        RemoteUser found = clientApi.findByLoginIdAndPassword(loginId, password);
        return Optional
                .ofNullable(found)
                .map(entityMapper::toModel);
    }
}
