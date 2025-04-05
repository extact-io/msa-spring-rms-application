package io.extact.msa.spring.rms.infrastructure.persistence.jpa;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import io.extact.msa.spring.rms.application.member.ReservationQueryService;
import io.extact.msa.spring.rms.domain.reservation.ReservationRepository;
import io.extact.msa.spring.rms.infrastructure.persistence.AbstractReservationRepositoryTest;

@DataJpaTest
@ActiveProfiles({ "test", "reservation-jpa" })
class ReservationJpaRepositoryTest extends AbstractReservationRepositoryTest {

    @Autowired
    private ReservationRepository repository;
    @Autowired
    private ReservationQueryService queryService;

    @Configuration(proxyBeanMethods = false)
    @Import(JpaRepositoryConfig.class)
    static class TestConfig {
    }

    @Override
    protected ReservationRepository repository() {
        return this.repository;
    }
    
    @Override
    protected ReservationQueryService queryService() {
        return this.queryService;
    }

    @Test
    @Override
    protected void testNextIdentity() {

        // when
        int firstTime = repository.nextIdentity();
        int secondTime = repository.nextIdentity();
        int thirdTime = repository.nextIdentity();

        // then
        assertThat(secondTime).isEqualTo(firstTime + 1);
        assertThat(thirdTime).isEqualTo(secondTime + 1);
    }
}
