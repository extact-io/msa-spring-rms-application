package io.extact.msa.spring.rms.infrastructure.persistence.remote.remote;

import java.time.Duration;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;

import io.extact.msa.spring.platform.fw.feature.profile.PersistenceProfileType;
import lombok.extern.slf4j.Slf4j;

@TestConfiguration(proxyBeanMethods = false)
@Slf4j
class TestcontainersConfig {

    @Bean
    @SuppressWarnings("resource")
    GenericContainer<?> stubContainer(Environment env) {
        return new GenericContainer<>(env.getProperty("rms.remote-stub.image"))
                .withExposedPorts(8081)
                .waitingFor(Wait.forHttp("/actuator/health/readiness").withStartupTimeout(Duration.ofSeconds(10)))
                .withEnv("RMS_LOG_SERVER_ENABLE", "true");
    }

    @Bean
    DynamicPropertyRegistrar remoteUrlRegistrar(GenericContainer<?> container, Environment env) {
        String destination = "http://" + container.getHost() + ":" + container.getFirstMappedPort() + "/remote";
        log.info("DESTINATION -> " + destination);
        String resourceName = resolveResourceName(env.getActiveProfiles());
        return registry -> registry.add("rms.persistence." + resourceName + ".remote.url", () -> destination);
    }

    String resolveResourceName(String[] activeProfiles) {
        return Stream.of(activeProfiles)
                .map(PersistenceProfileType.REMOTE::resolveEntityName)
                .flatMap(Optional::stream)
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException(
                        "could not be resolved from active profile -> " + activeProfiles));
    }

    static void followOutputContainerLog(GenericContainer<?> container) {
        Slf4jLogConsumer logConsumer = new Slf4jLogConsumer(log);
        container.followOutput(logConsumer);
    }
}
