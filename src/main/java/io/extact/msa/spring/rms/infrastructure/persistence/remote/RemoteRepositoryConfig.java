package io.extact.msa.spring.rms.infrastructure.persistence.remote;

import static io.extact.msa.spring.platform.fw.feature.profile.PersistenceProfileType.*;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.env.Environment;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import org.springframework.web.util.UriBuilderFactory;

import io.extact.msa.spring.platform.core.auth.client.LoginUserHeaderRequestInitializer;
import io.extact.msa.spring.platform.fw.domain.model.ModelValidator;
import io.extact.msa.spring.platform.fw.feature.profile.ConditionalOnAnyPersistenceProfile;
import io.extact.msa.spring.platform.fw.feature.validator.ValidatorConfig;
import io.extact.msa.spring.platform.fw.infrastructure.external.CustomUriBuilderFactory;
import io.extact.msa.spring.platform.fw.infrastructure.external.ErrorMessageDeserializer;
import io.extact.msa.spring.platform.fw.infrastructure.external.ExternalProperties;
import io.extact.msa.spring.platform.fw.infrastructure.external.RestClientErrorHandler;
import io.extact.msa.spring.platform.fw.infrastructure.external.converter.ConfigConversionServiceBuilder;
import io.extact.msa.spring.platform.fw.infrastructure.external.converter.ConfigMessageConveterBuilder;
import io.extact.msa.spring.platform.fw.infrastructure.persistence.DefaultModelEntityMapper;
import io.extact.msa.spring.rms.infrastructure.persistence.remote.item.RemoteItem;
import io.extact.msa.spring.rms.infrastructure.persistence.remote.item.RemoteItemClientApi;
import io.extact.msa.spring.rms.infrastructure.persistence.remote.item.RemoteItemRepository;
import io.extact.msa.spring.rms.infrastructure.persistence.remote.reservation.RemoteReservation;
import io.extact.msa.spring.rms.infrastructure.persistence.remote.reservation.RemoteReservationClientApi;
import io.extact.msa.spring.rms.infrastructure.persistence.remote.reservation.RemoteReservationRepository;
import io.extact.msa.spring.rms.infrastructure.persistence.remote.user.RemoteUser;
import io.extact.msa.spring.rms.infrastructure.persistence.remote.user.RemoteUserClientApi;
import io.extact.msa.spring.rms.infrastructure.persistence.remote.user.RemoteUserRepository;

@Configuration(proxyBeanMethods = false)
@ConditionalOnAnyPersistenceProfile(REMOTE)
@Import(ValidatorConfig.class)
public class RemoteRepositoryConfig {

    @Configuration(proxyBeanMethods = false)
    @Profile("item-remote")
    static class ItemRemoteConfiguration {

        @Bean("item")
        @ConfigurationProperties("rms.persistence.item.remote")
        ExternalProperties externalProperties() {
            return new ExternalProperties();
        }

        @Bean
        RemoteItemClientApi remoteItemClientApi(
                RestClient.Builder builder,
                @Qualifier("item") ExternalProperties prop,
                Environment env) {

            HttpServiceProxyFactory factory = httpServiceProxyFactory(builder, prop, env);
            return factory.createClient(RemoteItemClientApi.class);
        }

        @Bean
        RemoteItemRepository itemJpaRepository(RemoteItemClientApi client, ModelValidator validator) {
            return new RemoteItemRepository(
                    client,
                    new DefaultModelEntityMapper<>(RemoteItem::from, validator));
        }
    }

    @Configuration(proxyBeanMethods = false)
    @Profile("reservation-remote")
    static class ReservationRemoteConfiguration {

        @Bean("reservation")
        @ConfigurationProperties("rms.persistence.reservation.remote")
        ExternalProperties externalPropertiesForItem() {
            return new ExternalProperties();
        }

        @Bean
        RemoteReservationClientApi remoteReservationClientApi(
                RestClient.Builder builder,
                @Qualifier("reservation") ExternalProperties prop,
                Environment env) {

            HttpServiceProxyFactory factory = httpServiceProxyFactory(builder, prop, env);
            return factory.createClient(RemoteReservationClientApi.class);
        }

        @Bean
        RemoteReservationRepository remoteReservationRepository(RemoteReservationClientApi client, ModelValidator validator) {
            return new RemoteReservationRepository(
                    client,
                    new DefaultModelEntityMapper<>(RemoteReservation::from, validator));
        }
    }

    @Configuration(proxyBeanMethods = false)
    @Profile("user-remote")
    static class UserRemoteConfiguration {

        @Bean("user")
        @ConfigurationProperties("rms.persistence.user.remote")
        ExternalProperties externalPropertiesForItem() {
            return new ExternalProperties();
        }

        @Bean
        RemoteUserClientApi remoteUserClientApi(
                RestClient.Builder builder,
                @Qualifier("user") ExternalProperties prop,
                Environment env) {
            HttpServiceProxyFactory factory = httpServiceProxyFactory(builder, prop, env);
            return factory.createClient(RemoteUserClientApi.class);
        }

        @Bean
        RemoteUserRepository remoteUserRepository(RemoteUserClientApi client, ModelValidator validator) {
            return new RemoteUserRepository(
                    client,
                    new DefaultModelEntityMapper<>(RemoteUser::from, validator));
        }
    }

    static HttpServiceProxyFactory httpServiceProxyFactory(
            RestClient.Builder builder,
            ExternalProperties prop,
            Environment env) {

        ConversionService conversionService = ConfigConversionServiceBuilder
                .builder(prop)
                .build();
        UriBuilderFactory uriFactory = CustomUriBuilderFactory.newInstance()
                .env(env)
                .conversionService(conversionService)
                .uriTemplate(prop.getUrl())
                .build();
        HttpMessageConverter<Object> converter = ConfigMessageConveterBuilder
                .builder(prop)
                .build();

        RestClient restClient = builder
                .uriBuilderFactory(uriFactory)
                .messageConverters(converters -> converters.addFirst(converter))
                .requestInitializer(new LoginUserHeaderRequestInitializer())
                .defaultStatusHandler(new RestClientErrorHandler(new ErrorMessageDeserializer()))
                .build();

        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        return HttpServiceProxyFactory
                .builderFor(adapter)
                .conversionService(conversionService)
                .build();
    }
}
