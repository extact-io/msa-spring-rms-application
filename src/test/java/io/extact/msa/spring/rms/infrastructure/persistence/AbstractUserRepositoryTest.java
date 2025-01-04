package io.extact.msa.spring.rms.infrastructure.persistence;

import static io.extact.msa.spring.test.assertj.ToStringAssert.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import io.extact.msa.spring.platform.fw.exception.RmsPersistenceException;
import io.extact.msa.spring.rms.domain.user.UserRepository;
import io.extact.msa.spring.rms.domain.user.model.User;
import io.extact.msa.spring.rms.domain.user.model.User.UserCreatable;
import io.extact.msa.spring.rms.domain.user.model.UserId;
import io.extact.msa.spring.rms.domain.user.model.UserType;

@Transactional
@Rollback
public abstract class AbstractUserRepositoryTest {

    protected static final UserCreatable testCreator = new UserCreatable() {};

    private static final User user1 = testCreator
            .newInstance(
                    new UserId(1),
                    "member1",
                    "member1",
                    UserType.MEMBER,
                    "メンバー1",
                    "070-1111-2222",
                    "連絡先1");
    private static final User user2 = testCreator
            .newInstance(
                    new UserId(2),
                    "member2",
                    "member2",
                    UserType.MEMBER,
                    "メンバー2",
                    "080-1111-2222",
                    "連絡先2");
    private static final User user3 = testCreator
            .newInstance(
                    new UserId(3),
                    "admin",
                    "admin",
                    UserType.ADMIN,
                    "管理者",
                    "050-1111-2222",
                    "連絡先3");

    protected abstract UserRepository repository();

    @Test
    void testGet() {

        // given
        UserId userId = new UserId(1);
        // when
        Optional<User> actual = repository().find(userId);
        // then
        assertThat(actual).isPresent();
        assertThatToString(actual.get()).isEqualTo(user1);

        // given
        UserId notFoundId = new UserId(999);
        // when
        actual = repository().find(notFoundId);
        // then
        assertThat(actual).isNotPresent();
    }

    @Test
    void testGetAll() {
        // given
        List<User> expected = List.of(user1, user2, user3);
        // when
        List<User> actual = repository().findAll();
        // then
        assertThat(actual).containsExactlyElementsOf(expected);
    }

    @Test
    void testUpdate() {
        // given
        User updateUser = testCreator
                .newInstance(
                        new UserId(1),
                        "update1",
                        "update1",
                        UserType.ADMIN,
                        "メンバーUP",
                        "070-1111-9999",
                        "連絡先UP");
        // when
        repository().update(updateUser);
        //then
        assertThatToString(repository().find(updateUser.getId()).get()).isEqualTo(updateUser);
    }

    @Test
    void testUpdateOnNotFound() {
        // given
        User notFoundUser = testCreator
                .newInstance(
                        new UserId(999),
                        "update1",
                        "update1",
                        UserType.ADMIN,
                        "メンバーUP",
                        "070-1111-9999",
                        "連絡先UP");

        // when & then
        RmsPersistenceException exception = assertThrows(RmsPersistenceException.class, () -> {
            repository().update(notFoundUser);
        });
        assertThat(exception).hasMessageContaining("id:" + notFoundUser.getId().id());
    }

    @Test
    void testAdd() {
        // given
        User addUser = testCreator
                .newInstance(
                        new UserId(4),
                        "add-1",
                        "add-1",
                        UserType.MEMBER,
                        "メンバーADD",
                        "070-1111-8888",
                        "連絡先ADD");
        // when
        repository().add(addUser);
        // then
        assertThatToString(repository().find(addUser.getId()).get()).isEqualTo(addUser);
    }

    @Test
    void testDelete() {
        // given
        User deleteUser = repository().find(new UserId(1)).get();
        // when
        repository().delete(deleteUser);
        // then
        assertThat(repository().find(deleteUser.getId())).isEmpty();
    }

    @Test
    void testDeleteOnNotFound() {
        // given
        User notFoundUser = testCreator
                .newInstance(
                        new UserId(999),
                        "update1",
                        "update1",
                        UserType.ADMIN,
                        "メンバーUP",
                        "070-1111-9999",
                        "連絡先UP");
        // when & then
        RmsPersistenceException exception = assertThrows(RmsPersistenceException.class, () -> {
            repository().delete(notFoundUser);
        });
        assertThat(exception).hasMessageContaining("id:" + notFoundUser.getId().id());
    }

    @Test
    void testFindDpulicationData() {

        // given
        User foundUser = user1;
        // when
        Optional<User> result = repository().findDuplicationData(foundUser);
        // then
        assertThat(result).isPresent();

        // given
        User notFoundUser = testCreator
                .newInstance(
                        new UserId(999),
                        "unknown", // 重複なしのID
                        "update1",
                        UserType.ADMIN,
                        "メンバーUP",
                        "070-1111-9999",
                        "連絡先UP");
        // when
        result = repository().findDuplicationData(notFoundUser);
        // then
        assertThat(result).isNotPresent();
    }

    protected abstract void testNextIdentity();
}
