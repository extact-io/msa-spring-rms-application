package io.extact.msa.spring.rms.infrastructure.persistence.remote;

import jakarta.annotation.PostConstruct;

import org.springframework.core.env.Environment;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilderFactory;

import io.extact.msa.spring.platform.core.auth.client.LoginUserHeaderRequestInitializer;
import io.extact.msa.spring.platform.fw.infrastructure.external.CustomUriBuilderFactory;
import io.extact.msa.spring.platform.fw.infrastructure.external.ExternalProperties;
import io.extact.msa.spring.platform.fw.test.utils.TestAuthUtils;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RemoteRepositoryTestInitializer {

    private final ExternalProperties prop;
    private final Environment env;
    private final String resource;
    
    private RestClient client;
    
    @PostConstruct
    void init() {
        UriBuilderFactory uriFactory = CustomUriBuilderFactory.newInstance()
                .env(env)
                .uriTemplate(prop.getUrl() + "/" + resource)
                .build();
        client = RestClient.builder()
                .uriBuilderFactory(uriFactory)
                .requestInitializer(new LoginUserHeaderRequestInitializer())
                .build();
    }
    
    public void reset() {
        TestAuthUtils.signinByHeader(1, "MEMBER");
        client.get()
                .uri("/reset")
                .retrieve()
                .toBodilessEntity();
    }
}
