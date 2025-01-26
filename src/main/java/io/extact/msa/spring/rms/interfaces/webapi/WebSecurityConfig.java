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
import io.extact.msa.spring.rms.interfaces.webapi.admin.ItemAdminController;
import io.extact.msa.spring.rms.interfaces.webapi.admin.ReservationAdminController;
import io.extact.msa.spring.rms.interfaces.webapi.admin.UserAdminController;
import io.extact.msa.spring.rms.interfaces.webapi.member.ReservationMemberController;

@Configuration(proxyBeanMethods = false)
@Import(RmsJwtAuthConfig.class)
public class WebSecurityConfig implements WebMvcConfigurer {

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
