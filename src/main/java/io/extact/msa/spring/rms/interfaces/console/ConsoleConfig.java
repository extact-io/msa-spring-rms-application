package io.extact.msa.spring.rms.interfaces.console;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import io.extact.msa.spring.platform.core.env.EnvConfig;
import io.extact.msa.spring.platform.core.env.MainModuleInformation;
import io.extact.msa.spring.rms.application.admin.ItemAdminService;
import io.extact.msa.spring.rms.application.admin.ReservationAdminService;
import io.extact.msa.spring.rms.application.admin.UserAdminService;
import io.extact.msa.spring.rms.application.member.ReservationMemberService;
import io.extact.msa.spring.rms.application.universal.LoginService;
import io.extact.msa.spring.rms.interfaces.console.screen.MainScreenController;

@Configuration(proxyBeanMethods = false)
@Import({
    EnvConfig.class
})
public class ConsoleConfig {

    @Bean
    MainScreenController mainScreenController(
            LoginService loginService,
            ItemAdminService itemAdminService,
            ReservationAdminService reservationAdminService,
            UserAdminService userAdminService,
            ReservationMemberService reservationMemberService,
            MainModuleInformation moduleInfo) {

        return new MainScreenController(
                loginService,
                itemAdminService,
                reservationAdminService,
                userAdminService,
                reservationMemberService,
                moduleInfo);
    }

    @Bean
    MainScreenRunner mainScreenRunner(
            MainScreenController mainController,
            MainModuleInformation moduleInfo) {

        return new MainScreenRunner(mainController, moduleInfo);
    }
}
