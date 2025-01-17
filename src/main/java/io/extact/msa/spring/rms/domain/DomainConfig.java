package io.extact.msa.spring.rms.domain;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import io.extact.msa.spring.platform.fw.domain.model.ModelPropertySupportFactory;
import io.extact.msa.spring.platform.fw.domain.model.ModelValidator;
import io.extact.msa.spring.platform.fw.domain.service.DuplicateChecker;
import io.extact.msa.spring.platform.fw.domain.service.SimpleDuplicateChecker;
import io.extact.msa.spring.platform.fw.infrastructure.framework.model.DefaultModelPropertySupportFactory;
import io.extact.msa.spring.platform.fw.infrastructure.framework.validator.ValidatorConfig;
import io.extact.msa.spring.rms.domain.item.ItemCreator;
import io.extact.msa.spring.rms.domain.item.ItemRepository;
import io.extact.msa.spring.rms.domain.item.model.Item;
import io.extact.msa.spring.rms.domain.reservation.ReservationCreator;
import io.extact.msa.spring.rms.domain.reservation.ReservationDuplicateChecker;
import io.extact.msa.spring.rms.domain.reservation.ReservationRepository;
import io.extact.msa.spring.rms.domain.user.UserCreator;
import io.extact.msa.spring.rms.domain.user.UserRepository;
import io.extact.msa.spring.rms.domain.user.model.User;

@Configuration(proxyBeanMethods = false)
@Import({
    ValidatorConfig.class
})
public class DomainConfig {

    @Bean
    ItemCreator itemCreator(
                ItemRepository idGenerator,
                ModelValidator validator) {
        return new ItemCreator(idGenerator, validator, modelSupportFactory(validator));
    }

    @Bean
    DuplicateChecker<Item> itemDuplicateChecker(ItemRepository repository) {
        return new SimpleDuplicateChecker<Item>(repository);
    }

    @Bean
    ReservationCreator reservationCreator(
                ReservationRepository repository,
                ModelValidator validator) {
        return new ReservationCreator(repository, validator, modelSupportFactory(validator));
    }

    @Bean
    ReservationDuplicateChecker reservationDuplicateChecker(ReservationRepository repository) {
        return new ReservationDuplicateChecker(repository);
    }

    @Bean
    UserCreator userCreator(
                UserRepository repository,
                ModelValidator validator) {
        return new UserCreator(repository, validator, modelSupportFactory(validator));
    }

    @Bean
    DuplicateChecker<User> userDuplicateChecker(UserRepository repository) {
        return new SimpleDuplicateChecker<User>(repository);
    }

    private ModelPropertySupportFactory modelSupportFactory(ModelValidator validator) {
        return new DefaultModelPropertySupportFactory(validator);
    }
}
