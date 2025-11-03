package io.extact.msa.spring.rms.infrastructure.persistence.remote;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import io.extact.msa.spring.rms.application.member.ReserveItemQueryService;
import io.extact.msa.spring.rms.domain.item.model.ItemId;
import io.extact.msa.spring.rms.domain.reservation.ReservationRepository;
import io.extact.msa.spring.rms.domain.reservation.model.ReservationId;
import io.extact.msa.spring.rms.domain.reservation.model.ReservationPeriod;
import io.extact.msa.spring.rms.domain.user.model.UserId;
import io.extact.msa.spring.rms.infrastructure.persistence.AbstractReservationRepositoryTest;

public abstract class AbstractRemoteReservationRepositoryTest extends AbstractReservationRepositoryTest {

    @Autowired
    private ReservationRepository repository;
    @Autowired
    private ReserveItemQueryService queryService;

    @BeforeEach
    void beforeEach(@Autowired RemoteRepositoryTestInitializer initializer) {
        initializer.resetAndSignin("SYSTEM");
    }

    @Override
    protected ReservationRepository repository() {
        return this.repository;
    }

    @Test
    @Override
    protected void testNextIdentity() {

        // when
        ReservationId firstTime = repository.nextIdentity();
        LocalDateTime from = LocalDateTime.now().plusDays(1);
        LocalDateTime to = from.plusDays(1);
        repository.add(testCreator.newInstance(
                firstTime,
                new ReservationPeriod(from, to),
                "1st",
                new ItemId(1),
                new UserId(1)));

        ReservationId secondTime = repository.nextIdentity();
        from = from.plusDays(1);
        to = to.plusDays(1);
        repository.add(testCreator.newInstance(
                secondTime,
                new ReservationPeriod(from, to),
                "2nd",
                new ItemId(1),
                new UserId(1)));

        ReservationId thirdTime = repository.nextIdentity();
        from = from.plusDays(1);
        to = to.plusDays(1);
        repository.add(testCreator.newInstance(
                thirdTime,
                new ReservationPeriod(from, to),
                "3rd",
                new ItemId(1),
                new UserId(1)));

        // then
        assertThat(secondTime.id()).isEqualTo(firstTime.id() + 1);
        assertThat(thirdTime.id()).isEqualTo(secondTime.id() + 1);
    }

    @Override
    protected ReserveItemQueryService queryService() {
        return this.queryService;
    }
}
