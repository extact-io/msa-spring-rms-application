package io.extact.msa.spring.rms.infrastructure.persistence;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import io.extact.msa.spring.rms.infrastructure.persistence.file.FileRepositoryConfig;
import io.extact.msa.spring.rms.infrastructure.persistence.jpa.JpaRepositoryConfig;
import io.extact.msa.spring.rms.infrastructure.persistence.remote.RemoteRepositoryConfig;

@Configuration(proxyBeanMethods = false)
@Import({
    FileRepositoryConfig.class,
    JpaRepositoryConfig.class,
    RemoteRepositoryConfig.class })
public class PersistenceConfig {
}
