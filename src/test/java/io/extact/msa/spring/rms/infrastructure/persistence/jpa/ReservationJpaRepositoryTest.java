package io.extact.msa.spring.rms.infrastructure.persistence.jpa;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import io.extact.msa.spring.rms.application.member.ReserveItemQueryService;
import io.extact.msa.spring.rms.domain.reservation.ReservationRepository;
import io.extact.msa.spring.rms.domain.reservation.model.ReservationId;
import io.extact.msa.spring.rms.infrastructure.persistence.AbstractReservationRepositoryTest;

@DataJpaTest
@ActiveProfiles({ "test", "reservation-jpa" })
class ReservationJpaRepositoryTest extends AbstractReservationRepositoryTest {

    @Autowired
    private ReservationRepository repository;
    @Autowired
    private ReserveItemQueryService queryService;

    @Configuration(proxyBeanMethods = false)
    @Import(JpaRepositoryConfig.class)
    static class TestConfig {
    }

    @Override
    protected ReservationRepository repository() {
        return this.repository;
    }

    @Override
    protected ReserveItemQueryService queryService() {
        return this.queryService;
    }

    @Test
    @Override
    protected void testNextIdentity() {

        // when
        ReservationId firstTime = repository.nextIdentity();
        ReservationId secondTime = repository.nextIdentity();
        ReservationId thirdTime = repository.nextIdentity();

        // then
        assertThat(secondTime.id()).isEqualTo(firstTime.id() + 1);
        assertThat(thirdTime.id()).isEqualTo(secondTime.id() + 1);
    }
}
