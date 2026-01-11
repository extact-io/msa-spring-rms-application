package io.extact.msa.spring.rms.infrastructure.persistence.remote;

import static io.extact.msa.spring.platform.fw.feature.profile.PersistenceProfileType.*;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.client.RestClient;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import io.extact.msa.spring.platform.fw.domain.model.ModelValidator;
import io.extact.msa.spring.platform.fw.feature.profile.ConditionalOnAnyPersistenceProfile;
import io.extact.msa.spring.platform.fw.feature.validator.ValidatorConfig;
import io.extact.msa.spring.platform.fw.infrastructure.external.ExternalProperties;
import io.extact.msa.spring.platform.fw.infrastructure.external.customizer.DefaultRmsProxyFactoryCustomizer;
import io.extact.msa.spring.platform.fw.infrastructure.external.customizer.DefaultRmsRestClientCustomizer;
import io.extact.msa.spring.platform.fw.infrastructure.external.customizer.DefaultRmsRestClientObjectMapperCustomizer;
import io.extact.msa.spring.platform.fw.infrastructure.external.customizer.RmsProxyFactoryCustomizer;
import io.extact.msa.spring.platform.fw.infrastructure.external.customizer.RmsProxyFactorySourceCreator;
import io.extact.msa.spring.platform.fw.infrastructure.external.customizer.RmsProxyFactorySourceCreator.Source;
import io.extact.msa.spring.platform.fw.infrastructure.external.customizer.RmsRestClientCustomizer;
import io.extact.msa.spring.platform.fw.infrastructure.external.customizer.RmsRestClientObjectMapperCustomizer;
import io.extact.msa.spring.platform.fw.infrastructure.persistence.DefaultModelEntityMapper;
import io.extact.msa.spring.rms.infrastructure.persistence.remote.item.ItemQualifier;
import io.extact.msa.spring.rms.infrastructure.persistence.remote.item.RemoteItem;
import io.extact.msa.spring.rms.infrastructure.persistence.remote.item.RemoteItemClientApi;
import io.extact.msa.spring.rms.infrastructure.persistence.remote.item.RemoteItemRepository;
import io.extact.msa.spring.rms.infrastructure.persistence.remote.reservation.RemoteReservation;
import io.extact.msa.spring.rms.infrastructure.persistence.remote.reservation.RemoteReservationClientApi;
import io.extact.msa.spring.rms.infrastructure.persistence.remote.reservation.RemoteReservationRepository;
import io.extact.msa.spring.rms.infrastructure.persistence.remote.reservation.ReservationQualifier;
import io.extact.msa.spring.rms.infrastructure.persistence.remote.user.RemoteUser;
import io.extact.msa.spring.rms.infrastructure.persistence.remote.user.RemoteUserClientApi;
import io.extact.msa.spring.rms.infrastructure.persistence.remote.user.RemoteUserRepository;
import io.extact.msa.spring.rms.infrastructure.persistence.remote.user.UserQualifier;

@Configuration(proxyBeanMethods = false)
@ConditionalOnAnyPersistenceProfile(REMOTE)
@Import(ValidatorConfig.class)
public class RemoteRepositoryConfig {

    @Configuration(proxyBeanMethods = false)
    @Profile("item-remote")
    static class ItemRemoteConfiguration {

        @Bean
        @ItemQualifier
        @ConfigurationProperties("rms.persistence.item.remote")
        ExternalProperties externalProperties() {
            return new ExternalProperties();
        }

        @Bean
        @ItemQualifier
        @Order(Ordered.LOWEST_PRECEDENCE)
        RmsRestClientObjectMapperCustomizer defaultItemObjectMapperCustomizer(@ItemQualifier ExternalProperties props) {
            return new DefaultRmsRestClientObjectMapperCustomizer(props);
        }

        @Bean
        @ItemQualifier
        @Order(Ordered.LOWEST_PRECEDENCE)
        RmsRestClientCustomizer defaultItemRestClientCustomizer(
                @ItemQualifier ExternalProperties props,
                @ItemQualifier List<RmsRestClientObjectMapperCustomizer> mapperCustomizers) {
            return new DefaultRmsRestClientCustomizer(props, mapperCustomizers);
        }

        @Bean
        @ItemQualifier
        @Order(Ordered.LOWEST_PRECEDENCE)
        RmsProxyFactoryCustomizer defaultItemProxyFactoryCustomizer(
                RestClient.Builder builder, // RestClientAutoConfigurationでCustomierが提供済みのBuilderを使用する
                @ItemQualifier List<RmsRestClientCustomizer> customizers) {

            RmsProxyFactorySourceCreator creator = new RmsProxyFactorySourceCreator(builder, customizers);
            Source source = creator.create();
            return new DefaultRmsProxyFactoryCustomizer(source.restClient(), source.conversionService());
        }

        @Bean
        RemoteItemClientApi remoteItemClientApi(
                @ItemQualifier List<RmsProxyFactoryCustomizer> customizers) {

            HttpServiceProxyFactory.Builder builder = HttpServiceProxyFactory.builder();
            customizers.forEach(customizer -> customizer.customize(builder));

            HttpServiceProxyFactory factory = builder.build();
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

        @Bean
        @ReservationQualifier
        @ConfigurationProperties("rms.persistence.reservation.remote")
        ExternalProperties externalPropertiesForItem() {
            return new ExternalProperties();
        }

        @Bean
        @ReservationQualifier
        @Order(Ordered.LOWEST_PRECEDENCE)
        RmsRestClientObjectMapperCustomizer defaultItemObjectMapperCustomizer(@ReservationQualifier ExternalProperties props) {
            return new DefaultRmsRestClientObjectMapperCustomizer(props);
        }

        @Bean
        @ReservationQualifier
        @Order(Ordered.LOWEST_PRECEDENCE)
        RmsRestClientCustomizer defaultItemRestClientCustomizer(
                @ReservationQualifier ExternalProperties props,
                @ReservationQualifier List<RmsRestClientObjectMapperCustomizer> mapperCustomizers) {
            return new DefaultRmsRestClientCustomizer(props, mapperCustomizers);
        }

        @Bean
        @ReservationQualifier
        @Order(Ordered.LOWEST_PRECEDENCE)
        RmsProxyFactoryCustomizer defaultItemProxyFactoryCustomizer(
                RestClient.Builder builder, // RestClientAutoConfigurationでCustomierが提供済みのBuilderを使用する
                @ReservationQualifier List<RmsRestClientCustomizer> customizers) {

            RmsProxyFactorySourceCreator creator = new RmsProxyFactorySourceCreator(builder, customizers);
            Source source = creator.create();
            return new DefaultRmsProxyFactoryCustomizer(source.restClient(), source.conversionService());
        }

        @Bean
        RemoteReservationClientApi remoteReservationClientApi(
                @ReservationQualifier List<RmsProxyFactoryCustomizer> customizers) {

            HttpServiceProxyFactory.Builder builder = HttpServiceProxyFactory.builder();
            customizers.forEach(customizer -> customizer.customize(builder));

            HttpServiceProxyFactory factory = builder.build();
            return factory.createClient(RemoteReservationClientApi.class);
        }

        @Bean
        RemoteReservationRepository remoteReservationRepository(RemoteReservationClientApi client,
                ModelValidator validator) {
            return new RemoteReservationRepository(
                    client,
                    new DefaultModelEntityMapper<>(RemoteReservation::from, validator));
        }
    }

    @Configuration(proxyBeanMethods = false)
    @Profile("user-remote")
    static class UserRemoteConfiguration {

        @Bean
        @UserQualifier
        @ConfigurationProperties("rms.persistence.user.remote")
        ExternalProperties externalPropertiesForItem() {
            return new ExternalProperties();
        }

        @Bean
        @UserQualifier
        @Order(Ordered.LOWEST_PRECEDENCE)
        RmsRestClientObjectMapperCustomizer defaultItemObjectMapperCustomizer(@UserQualifier ExternalProperties props) {
            return new DefaultRmsRestClientObjectMapperCustomizer(props);
        }

        @Bean
        @UserQualifier
        @Order(Ordered.LOWEST_PRECEDENCE)
        RmsRestClientCustomizer defaultItemRestClientCustomizer(
                @UserQualifier ExternalProperties props,
                @UserQualifier List<RmsRestClientObjectMapperCustomizer> mapperCustomizers) {
            return new DefaultRmsRestClientCustomizer(props, mapperCustomizers);
        }

        @Bean
        @UserQualifier
        @Order(Ordered.LOWEST_PRECEDENCE)
        RmsProxyFactoryCustomizer defaultItemProxyFactoryCustomizer(
                RestClient.Builder builder, // RestClientAutoConfigurationでCustomierが提供済みのBuilderを使用する
                @UserQualifier List<RmsRestClientCustomizer> customizers) {

            RmsProxyFactorySourceCreator creator = new RmsProxyFactorySourceCreator(builder, customizers);
            Source source = creator.create();
            return new DefaultRmsProxyFactoryCustomizer(source.restClient(), source.conversionService());
        }

        @Bean
        RemoteUserClientApi remoteUserClientApi(
                @UserQualifier List<RmsProxyFactoryCustomizer> customizers) {

            HttpServiceProxyFactory.Builder builder = HttpServiceProxyFactory.builder();
            customizers.forEach(customizer -> customizer.customize(builder));

            HttpServiceProxyFactory factory = builder.build();
            return factory.createClient(RemoteUserClientApi.class);
        }

        @Bean
        RemoteUserRepository remoteUserRepository(RemoteUserClientApi client, ModelValidator validator) {
            return new RemoteUserRepository(
                    client,
                    new DefaultModelEntityMapper<>(RemoteUser::from, validator));
        }
    }
}
