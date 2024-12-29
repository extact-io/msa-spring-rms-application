package io.extact.msa.spring.item.infrastructure.file;

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

import io.extact.msa.spring.item.domain.RentalItemRepository;
import io.extact.msa.spring.item.domain.model.RentalItem;
import io.extact.msa.spring.item.infrastructure.RentalItemRepositoryTest;
import io.extact.msa.spring.item.infrastructure.file.RentalItemFileRepositoryTest.TestConfig;
import io.extact.msa.spring.platform.fw.infrastructure.persistence.file.ModelArrayMapper;
import io.extact.msa.spring.platform.fw.infrastructure.persistence.file.io.FileOperator;
import io.extact.msa.spring.platform.fw.infrastructure.persistence.file.io.LoadPathDeriver;
import io.extact.msa.spring.test.spring.NopTransactionManager;
import io.extact.msa.spring.test.spring.SelfRootContext;

@SpringBootTest(classes = { SelfRootContext.class, TestConfig.class }, webEnvironment = WebEnvironment.NONE)
@ActiveProfiles("file")
class RentalItemFileRepositoryTest extends RentalItemRepositoryTest {

    private ItemRepository repository;

    @TestConfiguration(proxyBeanMethods = false)
    @Import(RentalItemFileRepositoryConfig.class)
    static class TestConfig {

        @Bean
        @Primary
        @Scope("prototype")
        ItemRepository prototypeRentalItemFileRepository(Environment env, ModelArrayMapper<Item> mapper)
                throws IOException {
            LoadPathDeriver pathDeriver = new LoadPathDeriver(env); // Bean生成の都度ファイルを再配置する
            FileOperator fileOperator = new FileOperator(pathDeriver.derive(ItemFileRepository.FILE_ENTITY));
            return new ItemFileRepository(fileOperator, mapper);
        }

        @Bean
        PlatformTransactionManager nopTransactionManager() {
            return new NopTransactionManager();
        }
    }

    // prototypeスコープのためInjectionさせることで都度ファイルの初期が行われるようにする
    @BeforeEach
    void beforeEach(@Autowired ItemRepository repository) {
        this.repository = repository;
    }

    @Override
    protected ItemRepository repository() {
        return this.repository;
    }

    @Test
    @Override
    protected void testNextIdentity() {
        // when
        int firstTime = repository.nextIdentity();
        repository.add(Item.reconstruct(firstTime, "1st", ""));
        int secondTime = repository.nextIdentity();
        repository.add(Item.reconstruct(secondTime, "2nd", ""));
        int thirdTime = repository.nextIdentity();
        repository.add(Item.reconstruct(thirdTime, "3rd", ""));
        // then
        assertThat(secondTime).isEqualTo(firstTime + 1);
        assertThat(thirdTime).isEqualTo(secondTime + 1);
    }
}
