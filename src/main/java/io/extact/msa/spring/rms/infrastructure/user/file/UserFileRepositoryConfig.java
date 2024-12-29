package io.extact.msa.spring.rms.infrastructure.user.file;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import io.extact.msa.spring.platform.fw.domain.constraint.ValidationConfiguration;
import io.extact.msa.spring.platform.fw.infrastructure.persistence.file.ModelArrayMapper;
import io.extact.msa.spring.platform.fw.infrastructure.persistence.file.io.FileOperator;
import io.extact.msa.spring.platform.fw.infrastructure.persistence.file.io.LoadPathDeriver;
import io.extact.msa.spring.rms.domain.user.model.User;

@Configuration(proxyBeanMethods = false)
@Import(ValidationConfiguration.class)
@Profile("file")
class UserFileRepositoryConfig {

    @Bean
    ModelArrayMapper<User> userAccountArrayMapper() {
        return UserArrayMapper.INSTANCE;
    }

    @Bean
    UserFileRepository userFileRepository(Environment env, ModelArrayMapper<User> mapper) {
        LoadPathDeriver pathDeriver = new LoadPathDeriver(env);
        FileOperator fileOperator = new FileOperator(pathDeriver.derive(UserFileRepository.FILE_ENTITY));
        return new UserFileRepository(fileOperator, mapper);
    }
}
