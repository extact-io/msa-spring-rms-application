package io.extact.msa.spring.rms.domain;

import jakarta.validation.Validator;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import io.extact.msa.spring.platform.fw.domain.constraint.ValidationConfiguration;
import io.extact.msa.spring.platform.fw.domain.service.DuplicateChecker;
import io.extact.msa.spring.platform.fw.domain.service.SimpleDuplicateChecker;
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
    ValidationConfiguration.class
})
public class DominConfig {

    @Bean
    ItemCreator itemCreator(
                ItemRepository repository,
                Validator validator) {
        return new ItemCreator(repository, validator);
    }

    @Bean
    DuplicateChecker<Item> itemDuplicateChecker(ItemRepository repository) {
        return new SimpleDuplicateChecker<Item>(repository);
    }

    @Bean
    ReservationCreator reservationCreator(
                ReservationRepository repository,
                Validator validator) {
        return new ReservationCreator(repository, validator);
    }

    @Bean
    ReservationDuplicateChecker reservationDuplicateChecker(ReservationRepository repository) {
        return new ReservationDuplicateChecker(repository);
    }

    @Bean
    UserCreator userCreator(
                UserRepository repository,
                Validator validator) {
        return new UserCreator(repository, validator);
    }

    @Bean
    DuplicateChecker<User> userDuplicateChecker(UserRepository repository) {
        return new SimpleDuplicateChecker<User>(repository);
    }
}
