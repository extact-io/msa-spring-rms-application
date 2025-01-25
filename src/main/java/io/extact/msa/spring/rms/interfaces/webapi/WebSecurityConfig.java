package io.extact.msa.spring.rms.interfaces.webapi;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;

import io.extact.msa.spring.platform.core.auth.configure.AuthorizeHttpRequestCustomizer;
import io.extact.msa.spring.platform.core.auth.jwt.RmsJwtAuthConfig;

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
