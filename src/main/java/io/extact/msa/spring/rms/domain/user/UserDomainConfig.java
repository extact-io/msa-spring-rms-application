package io.extact.msa.spring.rms.domain.user;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.extact.msa.spring.platform.fw.domain.service.DuplicateChecker;
import io.extact.msa.spring.platform.fw.domain.service.SimpleDuplicateChecker;
import io.extact.msa.spring.rms.domain.user.model.User;

@Configuration(proxyBeanMethods = false)
class UserDomainConfig {

    @Bean
    DuplicateChecker<User> duplicateChecker(UserRepository repository) {
        return new SimpleDuplicateChecker<User>(repository);
    }
}
