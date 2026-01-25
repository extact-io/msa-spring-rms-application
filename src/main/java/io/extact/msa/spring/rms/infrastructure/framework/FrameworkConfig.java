package io.extact.msa.spring.rms.infrastructure.framework;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import io.extact.msa.spring.platform.fw.feature.auth.RdbAttributesProvider;
import io.extact.msa.spring.platform.fw.feature.auth.RdbAttributesProviderConfig;

@Configuration(proxyBeanMethods = false)
@Import(RdbAttributesProviderConfig.class)
public class FrameworkConfig {

    @Bean
    LoginUserAttributesAutoRegisterHandler loAttributesAutoRegister(RdbAttributesProvider provider) {
        return new LoginUserAttributesAutoRegisterHandler(provider);
    }
}
