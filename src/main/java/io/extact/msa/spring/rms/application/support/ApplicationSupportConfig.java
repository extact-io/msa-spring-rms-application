package io.extact.msa.spring.rms.application.support;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import io.extact.msa.spring.platform.core.async.AsyncConfig;

@Configuration(proxyBeanMethods = false)
@Import(AsyncConfig.class)
class ApplicationSupportConfig {
}
