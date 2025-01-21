package io.extact.msa.spring.rms.boundary.webapi;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;

import io.extact.msa.spring.platform.core.auth.configure.AuthorizeHttpRequestCustomizer;
import io.extact.msa.spring.platform.core.auth.jwt.RmsJwtAuthConfig;
import io.extact.msa.spring.platform.core.env.ActiveProfileResolver;
import io.extact.msa.spring.platform.core.env.MainModuleInformation;
import io.extact.msa.spring.rms.application.admin.ItemAdminService;
import io.extact.msa.spring.rms.application.admin.ReservationAdminService;
import io.extact.msa.spring.rms.application.admin.UserAdminService;
import io.extact.msa.spring.rms.boundary.webapi.admin.ItemAdminController;
import io.extact.msa.spring.rms.boundary.webapi.admin.ReservationAdminController;
import io.extact.msa.spring.rms.boundary.webapi.admin.UserAdminController;
import io.extact.msa.spring.rms.boundary.webapi.member.ReservationMemberController;

@Configuration(proxyBeanMethods = false)
@Import(RmsJwtAuthConfig.class)
public class WebSecurityConfig {

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
