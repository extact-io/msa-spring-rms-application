package io.extact.msa.spring.rms.application;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import io.extact.msa.spring.platform.core.async.AsyncConfig;
import io.extact.msa.spring.platform.core.async.AsyncInvoker;
import io.extact.msa.spring.platform.core.auth.context.DefaultLoginContext;
import io.extact.msa.spring.platform.core.auth.context.LoginContext;
import io.extact.msa.spring.platform.fw.application.event.ApplicationServiceEventPublisher;
import io.extact.msa.spring.platform.fw.domain.service.DuplicateChecker;
import io.extact.msa.spring.platform.fw.feature.event.EventPublisherConfig;
import io.extact.msa.spring.rms.application.admin.ItemAdminService;
import io.extact.msa.spring.rms.application.admin.ReservationAdminService;
import io.extact.msa.spring.rms.application.admin.UserAdminService;
import io.extact.msa.spring.rms.application.admin.event.ReservationDependencyEventListener;
import io.extact.msa.spring.rms.application.member.ReserveItemService;
import io.extact.msa.spring.rms.application.member.ReserveItemQueryService;
import io.extact.msa.spring.rms.application.support.ReservationModelComposer;
import io.extact.msa.spring.rms.application.universal.LoginService;
import io.extact.msa.spring.rms.application.universal.UserProfileService;
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
@Import({ AsyncConfig.class, // for ReservationModelComposer
        EventPublisherConfig.class })
public class ApplicationServiceConfig {

    // ---- for admin
    @Bean
    ReservationModelComposer modelComposer(
            ItemRepository itemRepository,
            UserRepository userRepository,
            AsyncInvoker asyncInvoker) {

        return new ReservationModelComposer(itemRepository, userRepository, asyncInvoker);
    }

    @Bean
    ItemAdminService itemAdminService(
            ItemCreator modelCreator,
            DuplicateChecker<Item> duplicateChecker,
            ItemRepository repository,
            ApplicationServiceEventPublisher eventPublisher) {

        return new ItemAdminService(modelCreator, duplicateChecker, repository, eventPublisher);
    }

    @Bean
    ReservationAdminService reservationAdminService(
            ReservationDuplicateChecker duplicateChecker,
            ReservationModelComposer modelComposer,
            ReservationRepository repository) {

        return new ReservationAdminService(duplicateChecker, modelComposer, repository);
    }

    @Bean
    UserAdminService userAdminService(
            UserCreator modelCreator,
            DuplicateChecker<User> duplicateChecker,
            UserRepository repository,
            ApplicationServiceEventPublisher eventPublisher) {

        return new UserAdminService(modelCreator, duplicateChecker, repository, eventPublisher);
    }

    // ---- for member
    @Bean
    ReserveItemService reservationMemberService(
            LoginContext loginContext,
            ReservationCreator modelCreator,
            ReservationModelComposer modelComposer,
            ReservationDuplicateChecker duplicateChecker,
            ReserveItemQueryService queryService,
            ReservationRepository reservationRepository,
            ItemRepository itemRepository,
            UserRepository userRepository) {

        return new ReserveItemService(
                loginContext,
                modelCreator,
                modelComposer,
                duplicateChecker,
                queryService,
                reservationRepository,
                itemRepository,
                userRepository);
    }

    // ---- for universal
    @Bean
    LoginService loginService(UserRepository repository) {
        return new LoginService(repository);
    }

    @Bean
    UserProfileService userProfileService(LoginContext loginContext, UserRepository repository) {
        return new UserProfileService(loginContext, repository);
    }

    // ---- for listener
    @Bean
    ReservationDependencyEventListener dependencyEventListener(ReservationRepository repository) {

        return new ReservationDependencyEventListener(repository);
    }

    // ---- for etc.
    @Bean
    LoginContext loginContext() {
        return new DefaultLoginContext();
    }

}
