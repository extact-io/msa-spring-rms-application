package io.extact.msa.spring.rms.boundary.webapi.admin;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.*;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import io.extact.msa.spring.PersistedTestData;
import io.extact.msa.spring.platform.core.auth.client.BearerTokenRequestInitializer;
import io.extact.msa.spring.platform.core.condition.EnableAutoConfigurationWithoutJpa;
import io.extact.msa.spring.platform.core.jwt.encode.JsonWebTokenGenerator;
import io.extact.msa.spring.platform.core.jwt.encode.JwtEncodeConfig;
import io.extact.msa.spring.platform.fw.exception.BusinessFlowException;
import io.extact.msa.spring.platform.fw.exception.BusinessFlowException.CauseType;
import io.extact.msa.spring.platform.fw.exception.RmsValidationException;
import io.extact.msa.spring.platform.fw.infrastructure.external.ErrorMessageDeserializer;
import io.extact.msa.spring.platform.fw.infrastructure.external.RestClientErrorHandler;
import io.extact.msa.spring.platform.fw.infrastructure.external.SecurityConstraintException;
import io.extact.msa.spring.platform.test.stub.auth.TestAuthUtils;
import io.extact.msa.spring.rms.WebApiApplication;
import io.extact.msa.spring.test.spring.LocalHostUriBuilderFactory;

/**
 * リクエスト受信からレスポンス送信までの一連の処理に対するテスト。
 * このテストケースには以下の観点も含まれている。
 * ・Reqeust → Command → Model → 物理モデルの項目マッピング（）
 * ・Response ← Model ← 物理モデルの項目マッピング（途中で誤っている場合は期待どおりのレスポンスは返らない）
 * <p>
 * 途中で項目の移送が誤っている場合は期待どおりのレスポンスは返らない。
 * レスポンスの全項目をassertして確認している。よってこのケースが通ることで各レイヤ間の各項目のマッピングが正しいことは
 * 担保される
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@EnableAutoConfigurationWithoutJpa
@ActiveProfiles({ "file-all", "test" })
@TestMethodOrder(OrderAnnotation.class)
public class ItemAdminControllerIntegrationTest {

    private static final ItemAdminResponse item1 = ItemAdminResponse.from(PersistedTestData.item1);
    private static final ItemAdminResponse item2 = ItemAdminResponse.from(PersistedTestData.item2);
    private static final ItemAdminResponse item3 = ItemAdminResponse.from(PersistedTestData.item3);
    private static final ItemAdminResponse item4 = ItemAdminResponse.from(PersistedTestData.item4);

    @Autowired
    private RentalItemClient client;

    @Configuration(proxyBeanMethods = false)
    @EnableWebSecurity(debug = true)
    @Import({
        WebApiApplication.class,
        JwtEncodeConfig.class })
    static class TestConfig {

        @Bean
        RentalItemClient itemClient(Environment env) {

            RestClient restClient = RestClient.builder()
                    .uriBuilderFactory(new LocalHostUriBuilderFactory(env))
                    .defaultStatusHandler(new RestClientErrorHandler(new ErrorMessageDeserializer()))
                    .requestInitializer(new BearerTokenRequestInitializer())
                    .build();

            RestClientAdapter adapter = RestClientAdapter.create(restClient);
            HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
            return factory.createClient(RentalItemClient.class);
        }
    }

    @BeforeEach
    void beforeEach(@Autowired JsonWebTokenGenerator generator) {
        TestAuthUtils.signinByJwt(generator, 1, "MEMBER");
    }

    @AfterEach
    void afterEach() {
        TestAuthUtils.signoutQuietly();
    }

    @Test
    @Order(1)
    void testGetAll() {
        // given
        List<ItemAdminResponse> expected = List.of(item1, item2, item3, item4);
        // when
        List<ItemAdminResponse> actual = client.getAll();
        // then
        assertThat(actual).containsExactlyElementsOf(expected);
    }

    @Test
    void testGetAllOnAuthenticationError() {
        // given
        SecurityContextHolder.clearContext();
        // when
        assertThatThrownBy(() -> client.getAll())
                // then
                .isInstanceOfSatisfying(SecurityConstraintException.class, thrown -> {
                    assertThat(thrown).hasMessageContaining("認証エラー");
                });
    }

    @Test
    @Order(3)
    void testAdd() {
        // given
        ItemAddRequest request = ItemAddRequest.builder()
                .serialNo("newNo")
                .itemName("追加アイテム")
                .build();
        // when
        ItemAdminResponse actual = client.add(request);
        // then
        assertThat(actual).isEqualTo(new ItemAdminResponse(5, "newNo", "追加アイテム"));
    }

    @Test
    void testAddOnParameterError() {
        // given
        ItemAddRequest request = ItemAddRequest.builder()
                .build(); // empty values
        // when
        assertThatThrownBy(() -> client.add(request))
                // then
                .isInstanceOfSatisfying(RmsValidationException.class, thrown -> {
                    assertThat(thrown.getErrorMessage().validationErrorItems()).hasSize(1);
                    assertThat(thrown.getDetailMessage()).contains("serialNo");
                });
    }

    @Test
    void testAddOnDuplicate() {
        // given
        ItemAddRequest request = ItemAddRequest.builder()
                .serialNo("A0004")
                .itemName("レンタル品5号")
                .build();
        // when
        assertThatThrownBy(() -> client.add(request))
                // then
                .isInstanceOfSatisfying(BusinessFlowException.class, thrown -> {
                    assertThat(thrown.getCauseType()).isEqualTo(CauseType.DUPLICATE);
                });
    }

    @Test
    void testAddOnAuthenticationError() {
        // given
        SecurityContextHolder.clearContext();
        ItemAddRequest request = ItemAddRequest.builder()
                .serialNo("newNo")
                .itemName("追加アイテム")
                .build();
        // when
        assertThatThrownBy(() -> client.add(request))
                // then
                .isInstanceOfSatisfying(SecurityConstraintException.class, thrown -> {
                    assertThat(thrown).hasMessageContaining("認証エラー");
                });
    }

    @Test
    @Order(4)
    void testUpdate() {
        // given
        ItemUpdateRequest request = ItemUpdateRequest.builder()
                .id(2)
                .serialNo("UPDATE-1")
                .itemName("UPDATE-2")
                .build();
        // when
        ItemAdminResponse actual = client.update(request);
        // then
        assertThat(actual).isEqualTo(new ItemAdminResponse(2, "UPDATE-1", "UPDATE-2"));
    }

    @Test
    void testUpdateOnParameterError() {
        // given
        ItemUpdateRequest request = ItemUpdateRequest.builder()
                .serialNo("@@@@@") // 使用不可文字
                .itemName("1234567890123456") // 桁数オーバー
                .build();
        // when
        assertThatThrownBy(() -> client.update(request))
                // then
                .isInstanceOfSatisfying(RmsValidationException.class, thrown -> {
                    assertThat(thrown.getErrorMessage().validationErrorItems()).hasSize(3);
                    assertThat(thrown.getDetailMessage()).contains("id", "serialNo", "itemName");
                });
    }

    @Test
    void testUpdateOnNotFound() {
        // given
        ItemUpdateRequest request = ItemUpdateRequest.builder()
                .id(9) // not exist id
                .serialNo("UPDATE-1")
                .itemName("UPDATE-2")
                .build();
        // when
        assertThatThrownBy(() -> client.update(request))
                // then
                .isInstanceOfSatisfying(BusinessFlowException.class, thrown -> {
                    assertThat(thrown.getCauseType()).isEqualTo(CauseType.NOT_FOUND);
                });
    }

    @Test
    void testUpdateOnDuplicate() {
        // given
        ItemUpdateRequest request = ItemUpdateRequest.builder()
                .id(2)
                .serialNo("A0004")
                .build();
        // when
        assertThatThrownBy(() -> client.update(request))
                // then
                .isInstanceOfSatisfying(BusinessFlowException.class, thrown -> {
                    assertThat(thrown.getCauseType()).isEqualTo(CauseType.DUPLICATE);
                });
    }

    @Test
    void testUpdateOnAuthenticationError() {
        // given
        SecurityContextHolder.clearContext();
        ItemUpdateRequest request = ItemUpdateRequest.builder()
                .id(2)
                .serialNo("UPDATE-1")
                .itemName("UPDATE-2")
                .build();
        // when
        assertThatThrownBy(() -> client.update(request))
                // then
                .isInstanceOfSatisfying(SecurityConstraintException.class, thrown -> {
                    assertThat(thrown).hasMessageContaining("認証エラー");
                });
    }

    @Test
    @Order(5)
    void testDelete() {
        // given
        int deleteId = 1;
        // when
        client.delete(deleteId);
        // then
        ItemAdminResponse deleted = client.getAll().stream()
                .filter(item -> item.id().equals(deleteId))
                .findFirst()
                .orElse(null);
        assertThat(deleted).isNull();
    }

    @Test
    void testDeleteOnParameterError() {
        // given
        int errorId = -1;
        // when
        assertThatThrownBy(() -> client.delete(errorId))
                // then
                .isInstanceOfSatisfying(RmsValidationException.class, thrown -> {
                    assertThat(thrown.getErrorMessage().validationErrorItems()).hasSize(1);
                    assertThat(thrown.getDetailMessage()).contains("itemId");
                });
    }

    @Test
    void testDeleteOnNotFound() {
        // given
        int notExistId = 999;
        // when
        assertThatThrownBy(() -> client.delete(notExistId))
                // then
                .isInstanceOfSatisfying(BusinessFlowException.class, thrown -> {
                    assertThat(thrown.getCauseType()).isEqualTo(CauseType.NOT_FOUND);
                });
    }

    @Test
    void testDeleteOnAuthenticationError() {
        // given
        SecurityContextHolder.clearContext();
        int deleteId = 1;
        // when
        assertThatThrownBy(() -> client.delete(deleteId))
                // then
                .isInstanceOfSatisfying(SecurityConstraintException.class, thrown -> {
                    assertThat(thrown).hasMessageContaining("認証エラー");
                });
    }


    // ---------------------------------------------------- inner classes.

    @HttpExchange("/admin/items")
    public interface RentalItemClient {

        @GetExchange
        List<ItemAdminResponse> getAll();

        @PostExchange
        ItemAdminResponse add(@RequestBody ItemAddRequest request);

        @PutExchange
        ItemAdminResponse update(@RequestBody ItemUpdateRequest request);

        @DeleteExchange("/{id}")
        void delete(@PathVariable("id") Integer itemId);
    }
}
