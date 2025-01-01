package io.extact.msa.spring.rms.interfaces.webapi;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import io.extact.msa.spring.platform.core.auth.configure.AuthorizeHttpRequestCustomizer;
import io.extact.msa.spring.platform.core.auth.jwt.RmsJwtAuthConfig;
import io.extact.msa.spring.platform.core.env.EnvConfig;
import io.extact.msa.spring.platform.core.env.MainModuleInformation;
import io.extact.msa.spring.platform.fw.web.RestControllerConfig;
import io.extact.msa.spring.rms.application.admin.ItemAdminService;
import io.extact.msa.spring.rms.application.admin.ReservationAdminService;
import io.extact.msa.spring.rms.application.admin.UserAdminService;
import io.extact.msa.spring.rms.interfaces.webapi.admin.ItemAdminController;
import io.extact.msa.spring.rms.interfaces.webapi.admin.ReservationAdminController;
import io.extact.msa.spring.rms.interfaces.webapi.admin.UserAdminController;
import io.extact.msa.spring.rms.interfaces.webapi.member.ReservationMemberController;

@Configuration(proxyBeanMethods = false)
@Import({
        EnvConfig.class,
        RestControllerConfig.class,
        RmsJwtAuthConfig.class
})
public class WebApiConfig implements WebMvcConfigurer {

    @Bean
    StartupLogRunner startupLogRunner(MainModuleInformation moduleInfo) {
        return new StartupLogRunner(moduleInfo);
    }

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

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {

        configurer.addPathPrefix("/admin",
                HandlerTypePredicate.forAssignableType(
                        ItemAdminController.class,
                        ReservationAdminController.class,
                        UserAdminController.class));

        configurer.addPathPrefix("/member",
                HandlerTypePredicate.forAssignableType(
                        ReservationMemberController.class));
    }

    @Bean
    AuthorizeHttpRequestCustomizer authorizeRequestCustomizer() {
        return (AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry configurer) -> configurer
                .requestMatchers("/admin").hasRole("admin")
                .requestMatchers("/member").hasRole("member")
                .requestMatchers("/profile").authenticated()
                .requestMatchers("/login").permitAll()
                .anyRequest().authenticated();
    }
}
