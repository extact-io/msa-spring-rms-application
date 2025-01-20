package io.extact.msa.spring.rms.infrastructure.persistence.file;

import static io.extact.msa.spring.platform.fw.infrastructure.framework.profile.PersistenceProfileType.*;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import io.extact.msa.spring.platform.fw.domain.model.ModelPropertySupportFactory;
import io.extact.msa.spring.platform.fw.domain.model.ModelValidator;
import io.extact.msa.spring.platform.fw.infrastructure.framework.model.DefaultModelPropertySupportFactory;
import io.extact.msa.spring.platform.fw.infrastructure.framework.profile.ConditionalOnAnyPersistenceProfile;
import io.extact.msa.spring.platform.fw.infrastructure.framework.validator.ValidatorConfig;
import io.extact.msa.spring.platform.fw.infrastructure.persistence.file.ModelArrayMapper;
import io.extact.msa.spring.platform.fw.infrastructure.persistence.file.io.FileOperator;
import io.extact.msa.spring.platform.fw.infrastructure.persistence.file.io.LoadPathDeriver;
import io.extact.msa.spring.rms.domain.item.model.Item;
import io.extact.msa.spring.rms.domain.reservation.model.Reservation;
import io.extact.msa.spring.rms.domain.user.model.User;
import io.extact.msa.spring.rms.infrastructure.persistence.file.item.ItemArrayMapper;
import io.extact.msa.spring.rms.infrastructure.persistence.file.item.ItemFileRepository;
import io.extact.msa.spring.rms.infrastructure.persistence.file.reservation.ReservationArrayMapper;
import io.extact.msa.spring.rms.infrastructure.persistence.file.reservation.ReservationFileRepository;
import io.extact.msa.spring.rms.infrastructure.persistence.file.user.UserArrayMapper;
import io.extact.msa.spring.rms.infrastructure.persistence.file.user.UserFileRepository;

@Configuration(proxyBeanMethods = false)
@ConditionalOnAnyPersistenceProfile(FILE)
@Import(ValidatorConfig.class)
public class FileRepositoryConfig {

    @Configuration(proxyBeanMethods = false)
    @Profile("item-file")
    class ItemFileConfiguration {
        @Bean
        ModelArrayMapper<Item> rentalItemArrayMapper(ModelValidator validator) {
            return new ItemArrayMapper(modelSupportFactory(validator));
        }
        @Bean
        ItemFileRepository itemFileRepository(Environment env, ModelArrayMapper<Item> mapper) {
            LoadPathDeriver pathDeriver = new LoadPathDeriver(env);
            FileOperator fileOperator = new FileOperator(pathDeriver.derive(ItemFileRepository.FILE_ENTITY));
            return new ItemFileRepository(fileOperator, mapper);
        }
    }

    @Configuration(proxyBeanMethods = false)
    @Profile("reservation-file")
    class ReservationFileConfiguration {
        @Bean
        ModelArrayMapper<Reservation> reservationArrayMapper(ModelValidator validator) {
            return new ReservationArrayMapper(modelSupportFactory(validator));
        }
        @Bean
        ReservationFileRepository reservationFileRepository(Environment env, ModelArrayMapper<Reservation> mapper) {
            LoadPathDeriver pathDeriver = new LoadPathDeriver(env);
            FileOperator fileOperator = new FileOperator(pathDeriver.derive(ReservationFileRepository.FILE_ENTITY));
            return new ReservationFileRepository(fileOperator, mapper);
        }
    }

    @Configuration(proxyBeanMethods = false)
    @Profile("user-file")
    class UserFileConfiguration {
        @Bean
        ModelArrayMapper<User> userAccountArrayMapper(ModelValidator validator) {
            return new UserArrayMapper(modelSupportFactory(validator));
        }
        @Bean
        UserFileRepository userFileRepository(Environment env, ModelArrayMapper<User> mapper) {
            LoadPathDeriver pathDeriver = new LoadPathDeriver(env);
            FileOperator fileOperator = new FileOperator(pathDeriver.derive(UserFileRepository.FILE_ENTITY));
            return new UserFileRepository(fileOperator, mapper);
        }
    }

    private ModelPropertySupportFactory modelSupportFactory(ModelValidator validator) {
        return new DefaultModelPropertySupportFactory(validator);
    }
}
