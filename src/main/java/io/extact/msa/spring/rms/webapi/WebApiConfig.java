package io.extact.msa.spring.rms.webapi;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import io.extact.msa.spring.platform.core.jwt.encode.JwtEncodeConfig;
import io.extact.msa.spring.platform.fw.interfaces.webapi.RestControllerConfig;
import io.extact.msa.spring.rms.application.admin.ItemAdminService;
import io.extact.msa.spring.rms.application.admin.ReservationAdminService;
import io.extact.msa.spring.rms.application.admin.UserAdminService;
import io.extact.msa.spring.rms.application.member.ReserveItemService;
import io.extact.msa.spring.rms.application.universal.LoginService;
import io.extact.msa.spring.rms.application.universal.UserProfileService;
import io.extact.msa.spring.rms.webapi.admin.ItemAdminController;
import io.extact.msa.spring.rms.webapi.admin.ReservationAdminController;
import io.extact.msa.spring.rms.webapi.admin.UserAdminController;
import io.extact.msa.spring.rms.webapi.member.ReserveItemController;
import io.extact.msa.spring.rms.webapi.universal.LoginController;
import io.extact.msa.spring.rms.webapi.universal.UserProfileController;

@Configuration(proxyBeanMethods = false)
public class WebApiConfig {

    @Import({
            RestControllerConfig.class,
            RmsRestControllerExceptionHandler.class,
            WebSecurityConfig.class,
            JwtEncodeConfig.class, // for LoginController
    })
    public static class WebApiContextConfigs {
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
    ReserveItemController itemReservationController(ReserveItemService service) {
        return new ReserveItemController(service);
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

    // --- for exception handler

    @Bean
    RmsRestControllerExceptionHandler rmsRestControllerExceptionHandler() {
        return new RmsRestControllerExceptionHandler();
    }
}
