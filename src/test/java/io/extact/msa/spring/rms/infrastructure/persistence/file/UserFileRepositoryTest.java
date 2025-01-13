package io.extact.msa.spring.rms.infrastructure.persistence.file;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;

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
import io.extact.msa.spring.rms.domain.user.UserRepository;
import io.extact.msa.spring.rms.domain.user.model.User;
import io.extact.msa.spring.rms.domain.user.model.UserId;
import io.extact.msa.spring.rms.domain.user.model.UserType;
import io.extact.msa.spring.rms.infrastructure.persistence.AbstractUserRepositoryTest;
import io.extact.msa.spring.rms.infrastructure.persistence.file.UserFileRepositoryTest.TestConfig;
import io.extact.msa.spring.rms.infrastructure.persistence.file.user.UserFileRepository;
import io.extact.msa.spring.test.spring.NopTransactionManager;
import io.extact.msa.spring.test.spring.SelfRootContext;

@SpringBootTest(classes = { SelfRootContext.class, TestConfig.class }, webEnvironment = WebEnvironment.NONE)
@ActiveProfiles("user-file")
class UserFileRepositoryTest extends AbstractUserRepositoryTest {

    private UserRepository repository;

    @TestConfiguration(proxyBeanMethods = false)
    @Import(FileRepositoryConfig.class)
    static class TestConfig {

        @Bean
        @Primary
        @Scope("prototype")
        UserRepository prototypeRentalUserFileRepository(Environment env, ModelArrayMapper<User> mapper)
                throws IOException {
            LoadPathDeriver pathDeriver = new LoadPathDeriver(env); // Bean生成の都度ファイルを再配置する
            FileOperator fileOperator = new FileOperator(pathDeriver.derive(UserFileRepository.FILE_ENTITY));
            return new UserFileRepository(fileOperator, mapper);
        }

        @Bean
        PlatformTransactionManager nopTransactionManager() {
            return new NopTransactionManager();
        }
    }

    // prototypeスコープのためInjectionさせることで都度ファイルの初期が行われるようにする
    @BeforeEach
    void beforeEach(@Autowired UserRepository repository) {
        this.repository = repository;
    }

    @Override
    protected UserRepository repository() {
        return this.repository;
    }

    @Test
    @Override
    protected void testNextIdentity() {

        // when
        int firstTime = repository.nextIdentity();
        repository.add(testCreator
                .newInstance(
                        new UserId(firstTime),
                        "seq-test",
                        "seq-test",
                        UserType.MEMBER,
                        "seq-test",
                        "070-1111-8888",
                        "seq-test"));

        int secondTime = repository.nextIdentity();
        repository.add(testCreator
                .newInstance(
                        new UserId(secondTime),
                        "seq-test",
                        "seq-test",
                        UserType.MEMBER,
                        "seq-test",
                        "070-1111-8888",
                        "seq-test"));

        int thirdTime = repository.nextIdentity();
        repository.add(testCreator
                .newInstance(
                        new UserId(thirdTime),
                        "seq-test",
                        "seq-test",
                        UserType.MEMBER,
                        "seq-test",
                        "070-1111-8888",
                        "seq-test"));

        // then
        assertThat(secondTime).isEqualTo(firstTime + 1);
        assertThat(thirdTime).isEqualTo(secondTime + 1);
    }
}
