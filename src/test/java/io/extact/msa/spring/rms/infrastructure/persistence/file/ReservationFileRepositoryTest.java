package io.extact.msa.spring.rms.infrastructure.persistence.file;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;

import io.extact.msa.spring.platform.fw.infrastructure.persistence.file.ModelArrayMapper;
import io.extact.msa.spring.platform.fw.infrastructure.persistence.file.io.FileOperator;
import io.extact.msa.spring.platform.fw.infrastructure.persistence.file.io.LoadPathDeriver;
import io.extact.msa.spring.rms.domain.item.model.ItemId;
import io.extact.msa.spring.rms.domain.reservation.ReservationRepository;
import io.extact.msa.spring.rms.domain.reservation.model.Reservation;
import io.extact.msa.spring.rms.domain.reservation.model.ReservationId;
import io.extact.msa.spring.rms.domain.reservation.model.ReservationPeriod;
import io.extact.msa.spring.rms.domain.user.model.UserId;
import io.extact.msa.spring.rms.infrastructure.persistence.AbstractReservationRepositoryTest;
import io.extact.msa.spring.rms.infrastructure.persistence.file.ReservationFileRepositoryTest.TestConfig;
import io.extact.msa.spring.rms.infrastructure.persistence.file.reservation.ReservationFileRepository;
import io.extact.msa.spring.test.spring.NopTransactionManager;
import io.extact.msa.spring.test.spring.SelfRootContext;

@SpringBootTest(classes = { SelfRootContext.class, TestConfig.class }, webEnvironment = WebEnvironment.NONE)
@ActiveProfiles("reservation-file")
class ReservationFileRepositoryTest extends AbstractReservationRepositoryTest {

    private ReservationRepository repository;

    @TestConfiguration(proxyBeanMethods = false)
    @Import(FileRepositoryConfig.class)
    static class TestConfig {

        @Bean
        @Primary
        @Scope("prototype")
        ReservationRepository prototypeReservationFileRepository(Environment env, ModelArrayMapper<Reservation> mapper)
                throws IOException {
            LoadPathDeriver pathDeriver = new LoadPathDeriver(env); // Bean生成の都度ファイルを再配置する
            FileOperator fileOperator = new FileOperator(pathDeriver.derive(ReservationFileRepository.FILE_ENTITY));
            return new ReservationFileRepository(fileOperator, mapper);
        }

        @Bean
        PlatformTransactionManager nopTransactionManager() {
            return new NopTransactionManager();
        }
    }

    // prototypeスコープのためInjectionさせることで都度ファイルの初期が行われるようにする
    @BeforeEach
    void beforeEach(@Autowired ReservationRepository repository) {
        this.repository = repository;
    }

    @Override
    protected ReservationRepository repository() {
        return this.repository;
    }

    @Test
    @Override
    protected void testNextIdentity() {

        // when
        int firstTime = repository.nextIdentity();
        LocalDateTime from = LocalDateTime.now().plusDays(1);
        LocalDateTime to = from.plusDays(1);
        repository.add(testCreator.newInstance(
                new ReservationId(firstTime),
                new ReservationPeriod(from, to),
                "1st",
                new ItemId(1),
                new UserId(1)));

        int secondTime = repository.nextIdentity();
        from = from.plusDays(1);
        to = to.plusDays(1);
        repository.add(testCreator.newInstance(
                new ReservationId(secondTime),
                new ReservationPeriod(from, to),
                "2nd",
                new ItemId(1),
                new UserId(1)));

        int thirdTime = repository.nextIdentity();
        from = from.plusDays(1);
        to = to.plusDays(1);
        repository.add(testCreator.newInstance(
                new ReservationId(thirdTime),
                new ReservationPeriod(from, to),
                "3rd",
                new ItemId(1),
                new UserId(1)));

        // then
        assertThat(secondTime).isEqualTo(firstTime + 1);
        assertThat(thirdTime).isEqualTo(secondTime + 1);
    }
}
