package io.extact.msa.spring.rms.interfaces.webapi;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import io.extact.msa.spring.platform.core.env.ActiveProfileResolver;
import io.extact.msa.spring.platform.core.env.EnvConfig;
import io.extact.msa.spring.platform.core.env.MainModuleInformation;
import io.extact.msa.spring.platform.fw.web.RestControllerConfig;
import io.extact.msa.spring.rms.application.admin.ItemAdminService;
import io.extact.msa.spring.rms.application.admin.ReservationAdminService;
import io.extact.msa.spring.rms.application.admin.UserAdminService;
import io.extact.msa.spring.rms.application.member.ItemReservationService;
import io.extact.msa.spring.rms.application.universal.LoginService;
import io.extact.msa.spring.rms.application.universal.UserProfileService;
import io.extact.msa.spring.rms.interfaces.webapi.admin.ItemAdminController;
import io.extact.msa.spring.rms.interfaces.webapi.admin.ReservationAdminController;
import io.extact.msa.spring.rms.interfaces.webapi.admin.UserAdminController;
import io.extact.msa.spring.rms.interfaces.webapi.member.ItemReservationController;
import io.extact.msa.spring.rms.interfaces.webapi.universal.LoginController;
import io.extact.msa.spring.rms.interfaces.webapi.universal.UserProfileController;

@Configuration(proxyBeanMethods = false)
@Import({
        EnvConfig.class,
        RestControllerConfig.class,
        WebSecurityConfig.class
})
public class WebApiConfig {

    @Bean
    StartupLogRunner startupLogRunner(MainModuleInformation moduleInfo, ActiveProfileResolver profileResolver) {
        return new StartupLogRunner(moduleInfo, profileResolver);
    }

    // --- for admin

    @Bean
    ItemAdminController itemAdminController(ItemAdminService service) {
        return new ItemAdminController(service);
    }

    @Bean
    ReservationAdminController reservationAdminController(ReservationAdminService service) {
        return new ReservationAdminController(service);
    }

    @Bean
    UserAdminController userAdminController(UserAdminService service) {
        return new UserAdminController(service);
    }

    // --- for member

    @Bean
    ItemReservationController itemReservationController(ItemReservationService service) {
        return new ItemReservationController(service);
    }

    // --- for universal

    @Bean
    LoginController loginController(LoginService service) {
        return new LoginController(service);
    }

    @Bean
    UserProfileController userProfileController(UserProfileService service) {
        return new UserProfileController(service);
    }
}
