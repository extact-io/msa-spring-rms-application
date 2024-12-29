package io.extact.msa.spring.rms.infrastructure.reservation.file;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import io.extact.msa.spring.platform.fw.domain.constraint.ValidationConfiguration;
import io.extact.msa.spring.platform.fw.infrastructure.persistence.file.ModelArrayMapper;
import io.extact.msa.spring.platform.fw.infrastructure.persistence.file.io.FileOperator;
import io.extact.msa.spring.platform.fw.infrastructure.persistence.file.io.LoadPathDeriver;
import io.extact.msa.spring.rms.domain.reservation.model.Reservation;

@Configuration(proxyBeanMethods = false)
@Import(ValidationConfiguration.class)
@Profile("file")
class ReservationFileRepositoryConfig {

    @Bean
    ModelArrayMapper<Reservation> reservationArrayMapper() {
        return ReservationArrayMapper.INSTANCE;
    }

    @Bean
    ReservationFileRepository reservationFileRepository(Environment env, ModelArrayMapper<Reservation> mapper) {
        LoadPathDeriver pathDeriver = new LoadPathDeriver(env);
        FileOperator fileOperator = new FileOperator(pathDeriver.derive(ReservationFileRepository.FILE_ENTITY));
        return new ReservationFileRepository(fileOperator, mapper);
    }
}
