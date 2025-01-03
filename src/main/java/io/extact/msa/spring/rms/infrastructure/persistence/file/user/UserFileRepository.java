package io.extact.msa.spring.rms.infrastructure.persistence.file.user;

import java.util.Optional;

import io.extact.msa.spring.platform.fw.infrastructure.persistence.file.AbstractFileRepository;
import io.extact.msa.spring.platform.fw.infrastructure.persistence.file.ModelArrayMapper;
import io.extact.msa.spring.platform.fw.infrastructure.persistence.file.io.FileOperator;
import io.extact.msa.spring.rms.domain.user.UserRepository;
import io.extact.msa.spring.rms.domain.user.model.User;

public class UserFileRepository extends AbstractFileRepository<User> implements UserRepository {

    public static final String FILE_ENTITY = "user";

    public UserFileRepository(FileOperator fileReadWriter, ModelArrayMapper<User> mapper) {
        super(fileReadWriter, mapper);
    }

    @Override
    public String getEntityName() {
        return FILE_ENTITY;
    }

    @Override
    public Optional<User> findDuplicationData(User checkModel) {
        return this.findAll().stream()
                .filter(account -> account.getLoginId().equals(checkModel.getLoginId()))
                .findFirst();
    }

    @Override
    public Optional<User> findByLoginIdAndPasswod(String loginId, String password) {
        return this.findAll().stream()
                .filter(account -> account.getLoginId().equals(loginId))
                .filter(account -> account.getPassword().equals(password))
                .findFirst();
    }
}
