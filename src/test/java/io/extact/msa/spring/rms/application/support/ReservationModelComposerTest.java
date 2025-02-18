package io.extact.msa.spring.rms.application.support;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import jakarta.validation.Valid;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import io.extact.msa.spring.PersistedTestData;
import io.extact.msa.spring.platform.core.async.AsyncConfig;
import io.extact.msa.spring.platform.core.async.AsyncInvoker;
import io.extact.msa.spring.platform.fw.domain.model.Identity;
import io.extact.msa.spring.platform.fw.exception.BusinessFlowException;
import io.extact.msa.spring.platform.fw.exception.BusinessFlowException.CauseType;
import io.extact.msa.spring.rms.domain.item.ItemRepository;
import io.extact.msa.spring.rms.domain.item.model.Item;
import io.extact.msa.spring.rms.domain.item.model.ItemId;
import io.extact.msa.spring.rms.domain.reservation.model.Reservation;
import io.extact.msa.spring.rms.domain.reservation.model.Reservation.ReservationCreatable;
import io.extact.msa.spring.rms.domain.reservation.model.ReservationId;
import io.extact.msa.spring.rms.domain.reservation.model.ReservationPeriod;
import io.extact.msa.spring.rms.domain.reservation.model.ReservationModelView;
import io.extact.msa.spring.rms.domain.user.UserRepository;
import io.extact.msa.spring.rms.domain.user.model.User;
import io.extact.msa.spring.rms.domain.user.model.UserId;
import io.extact.msa.spring.test.assertj.ToStringAssert;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@ActiveProfiles("test")
class ReservationModelComposerTest {

    private static final ReservationCreatable reservationCreator = new ReservationCreatable() {};

    private static final Reservation ok_reservation = reservationCreator.newInstance(
            new ReservationId(1),
            new ReservationPeriod(LocalDateTime.of(2020, 4, 1, 10, 0), LocalDateTime.of(2020, 4, 1, 12, 0)),
            "メモ1",
            new ItemId(1),
            new UserId(1));

    private static final Reservation all_ng_reservation = reservationCreator.newInstance( // itemとuser両方not_found
            new ReservationId(2),
            new ReservationPeriod(LocalDateTime.of(2020, 4, 1, 16, 0), LocalDateTime.of(2020, 4, 1, 18, 0)),
            "メモ2",
            new ItemId(999),
            new UserId(999));
    private static final Reservation user_ng_reservation = reservationCreator.newInstance(
            new ReservationId(3),
            new ReservationPeriod(LocalDateTime.of(2099, 4, 1, 10, 0), LocalDateTime.of(2099, 4, 1, 12, 0)),
            "メモ3",
            new ItemId(1),
            new UserId(999));

    private static final ReservationComposeModel ok_expected = new ReservationComposeModel(
            ok_reservation,
            PersistedTestData.item1,
            PersistedTestData.user1);

    @Autowired
    private ReservationModelComposer composer;

    @Configuration(proxyBeanMethods = false)
    @Import(AsyncConfig.class)
    static class TestConfig {

        @Bean
        ReservationModelComposer reservationModelComposer(AsyncInvoker asyncInvoker) {
            return new ReservationModelComposer(
                    new ItemRepositoryStub(),
                    new UserRepositoryStub(),
                    asyncInvoker);
        }
    }

    @Test
    void testComposeModel() {
        // given
        ReservationModelView reservation = ok_reservation;
        // when
        ReservationComposeModel actual = composer.composeModel(reservation);
        // then
        ToStringAssert.assertThatToString(actual).isEqualTo(ok_expected);
    }

    @Test
    void testComposeModelOnItemNotFound() {
        // given
        ReservationModelView reservation = all_ng_reservation;
        // when
        BusinessFlowException thrown = assertThrows(BusinessFlowException.class, () -> {
            composer.composeModel(reservation); // UserよりもItemが先に評価される
        });
        // then
        assertThat(thrown).hasMessageContaining("target item");
        assertThat(thrown.getCauseType()).isEqualTo(CauseType.NOT_FOUND);
    }

    @Test
    void testComposeModelOnUserNotFound() {
        // given
        ReservationModelView reservation = user_ng_reservation;
        // when
        BusinessFlowException thrown = assertThrows(BusinessFlowException.class, () -> {
            composer.composeModel(reservation);
        });
        // then
        assertThat(thrown).hasMessageContaining("target user");
        assertThat(thrown.getCauseType()).isEqualTo(CauseType.NOT_FOUND);
    }

    // ------------------------------------------------------- stub classes

    static class ItemRepositoryStub implements ItemRepository {

        @Override
        public Optional<Item> find(Identity id) {
            if (id.id() == 999) {
                return Optional.empty();
            }
            return Optional.of(PersistedTestData.item1);
        }

        @Override
        public List<Item> findAll() {
            return null;
        }

        @Override
        public void add(@Valid Item model) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void update(@Valid Item model) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void delete(@Valid Item model) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Item> findDuplicationData(Item checkModel) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int nextIdentity() {
            throw new UnsupportedOperationException();
        }
    }

    static class UserRepositoryStub implements UserRepository {

        @Override
        public Optional<User> find(Identity id) {
            if (id.id() == 999) {
                return Optional.empty();
            }
            return Optional.of(PersistedTestData.user1);
        }

        @Override
        public List<User> findAll() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(@Valid User model) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void update(@Valid User model) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void delete(@Valid User model) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<User> findDuplicationData(User checkModel) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int nextIdentity() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<User> findByLoginIdAndPassword(String loginId, String password) {
            throw new UnsupportedOperationException();
        }
    }
}
