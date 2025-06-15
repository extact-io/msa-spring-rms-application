package io.extact.msa.spring.rms.webapi.admin;

import static io.extact.msa.spring.rms.PersistedTestData.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.extact.msa.spring.platform.core.env.EnvConfig;
import io.extact.msa.spring.platform.fw.exception.BusinessFlowException;
import io.extact.msa.spring.platform.fw.exception.BusinessFlowException.CauseType;
import io.extact.msa.spring.rms.application.admin.ItemAddCommand;
import io.extact.msa.spring.rms.application.admin.ItemAdminService;
import io.extact.msa.spring.rms.application.admin.ItemUpdateCommand;
import io.extact.msa.spring.rms.domain.item.model.Item.ItemCreatable;
import io.extact.msa.spring.rms.domain.item.model.ItemId;
import io.extact.msa.spring.rms.webapi.WebApiConfig.WebApiContextConfigs;
import io.extact.msa.spring.rms.webapi.admin.ItemAddRequest;
import io.extact.msa.spring.rms.webapi.admin.ItemAdminController;
import io.extact.msa.spring.rms.webapi.admin.ItemUpdateRequest;

/**
 * Controllerの単体テストクラス。
 * 以下の機能が有効になっている。<br>
 * ・ControllerAdvice
 * ・Spring Security
 * ・Method Validation
 * このテストケースには以下の観点も含まれている（よって他で個別にやる必要はない）
 * ・Request  → Commadの項目マッピングの確認
 * ・Response ← Modelの項目マッピングの確認
 */
@WebMvcTest
@ActiveProfiles("test")
class ItemAdminControllerTest {

    private static final ItemCreatable testCreator = new ItemCreatable() {
    };

    @Autowired
    private MockMvcTester mockMvc;
    @Autowired
    private ObjectMapper mapper;
    @MockitoBean
    private ItemAdminService itemService;

    @Configuration(proxyBeanMethods = false)
    @Import({
            EnvConfig.class,
            WebApiContextConfigs.class
    })
    static class TestConfig {
        @Bean
        ItemAdminController itemAdminController(ItemAdminService service) {
            return new ItemAdminController(service);
        }
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAll() throws Exception {

        // given
        when(itemService.getAll())
                .thenReturn(List.of(item1, item2, item3, item4));

        // when
        MvcTestResult result = mockMvc
                .get()
                .uri("/admin/items")
                .exchange();

        // then
        assertThat(result)
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.length()", p -> p.assertThat().isEqualTo(4))
                .hasPathSatisfying("$[0].id", p -> p.assertThat().isEqualTo(item1.getId().id()))
                .hasPathSatisfying("$[0].serialNo", p -> p.assertThat().isEqualTo(item1.getSerialNo()))
                .hasPathSatisfying("$[0].itemName", p -> p.assertThat().isEqualTo(item1.getItemName())); // 2件目以降の確認は省略
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllReturnEmpty() throws Exception {

        // given
        when(itemService.getAll())
                .thenReturn(List.of());

        // when
        MvcTestResult result = mockMvc
                .get()
                .uri("/admin/items")
                .exchange();

        // then
        assertThat(result)
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.length()", p -> p.assertThat().isEqualTo(0));
    }

    @Test
    void testGetAllOnAuthenticationError() throws Exception {

        // given
        // @WithMockUserなし

        // when
        MvcTestResult result = mockMvc
                .get()
                .uri("/admin/items")
                .exchange();

        // then
        assertThat(result)
                .hasStatus(HttpStatus.UNAUTHORIZED);
        verify(itemService, never()).getAll();

    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testAdd() throws Exception {

        // given
        ItemAddRequest req = ItemAddRequest.builder()
                .serialNo("newNo")
                .itemName("追加アイテム")
                .build();
        String body = mapper.writeValueAsString(req);

        ItemAddCommand shouldBePassed = req.toCommand();
        when(itemService.add(shouldBePassed))
                .thenReturn(testCreator.newInstance(new ItemId(5), req.serialNo(), req.itemName()));

        // when
        MvcTestResult result = mockMvc
                .post()
                .uri("/admin/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .exchange();

        // then
        assertThat(result)
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.id", p -> p.assertThat().isEqualTo(5))
                .hasPathSatisfying("$.serialNo", p -> p.assertThat().isEqualTo("newNo"))
                .hasPathSatisfying("$.itemName", p -> p.assertThat().isEqualTo("追加アイテム"));
        // -- JSONデシリアライズを使うのであれば↓のようにも簡潔にできる
        //.convertTo(ItemAdminResponse.class)
        //.extracting("id", "serialNo", "itemName")
        //.containsExactly(5, "newNo", "追加アイテム");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testAddOnParameterError() throws Exception {

        // given
        ItemAddRequest request = ItemAddRequest.builder()
                .build(); // empty value
        String body = mapper.writeValueAsString(request);

        // when
        MvcTestResult result = mockMvc
                .post()
                .uri("/admin/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .exchange();

        // then
        assertThat(result)
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyText()
                .contains("パラメーターエラーが発生しました", "serialNo");

        verify(itemService, never()).add(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testAddOnDuplicate() throws Exception {

        // given
        ItemAddRequest req = ItemAddRequest.builder()
                .serialNo("A0004")
                .itemName("レンタル品5号")
                .build();
        String body = mapper.writeValueAsString(req);

        ItemAddCommand shouldBePassed = req.toCommand();
        when(itemService.add(shouldBePassed))
                .thenThrow(new BusinessFlowException("from mock", CauseType.DUPLICATE));

        // when
        MvcTestResult result = mockMvc
                .post()
                .uri("/admin/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .exchange();

        // then
        assertThat(result)
                .hasStatus(HttpStatus.CONFLICT)
                .bodyText()
                .contains("DUPLICATE");
    }

    @Test
    void testAddOnAuthenticationError() throws Exception {

        // given
        ItemAddRequest req = ItemAddRequest.builder()
                .serialNo("newNo")
                .itemName("追加アイテム")
                .build();
        String body = mapper.writeValueAsString(req);

        // when
        MvcTestResult result = mockMvc
                .post()
                .uri("/admin/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .exchange();

        // then
        assertThat(result)
                .hasStatus(HttpStatus.UNAUTHORIZED);
        verify(itemService, never()).add(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdate() throws Exception {

        // given
        ItemUpdateRequest req = ItemUpdateRequest.builder()
                .id(2)
                .serialNo("UPDATE-1")
                .itemName("UPDATE-2")
                .build();
        String body = mapper.writeValueAsString(req);

        ItemUpdateCommand shouldBePassed = req.toCommand();
        when(itemService.update(shouldBePassed))
                .thenReturn(testCreator.newInstance(new ItemId(5), req.serialNo(), req.itemName()));

        // when
        MvcTestResult result = mockMvc
                .put()
                .uri("/admin/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .exchange();

        // then
        assertThat(result)
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.id", p -> p.assertThat().isEqualTo(5))
                .hasPathSatisfying("$.serialNo", p -> p.assertThat().isEqualTo("UPDATE-1"))
                .hasPathSatisfying("$.itemName", p -> p.assertThat().isEqualTo("UPDATE-2"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateOnParameterError() throws Exception {

        // given
        ItemUpdateRequest req = ItemUpdateRequest.builder()
                .serialNo("@@@@@") // 使用不可文字
                .itemName("1234567890123456") // 桁数オーバー
                .build();
        String body = mapper.writeValueAsString(req);

        // when
        MvcTestResult result = mockMvc
                .put()
                .uri("/admin/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .exchange();

        // then
        assertThat(result)
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyText()
                .contains("パラメーターエラーが発生しました", "id", "serialNo", "itemName");
        verify(itemService, never()).update(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateOnNotFound() throws Exception {

        // given
        ItemUpdateRequest req = ItemUpdateRequest.builder()
                .id(9) // not exist id
                .serialNo("UPDATE-1")
                .itemName("UPDATE-2")
                .build();
        String body = mapper.writeValueAsString(req);

        ItemUpdateCommand shouldBePassed = req.toCommand();
        when(itemService.update(shouldBePassed))
                .thenThrow(new BusinessFlowException("from mock", CauseType.NOT_FOUND));

        // when
        MvcTestResult result = mockMvc
                .put()
                .uri("/admin/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .exchange();

        // then
        assertThat(result)
                .hasStatus(HttpStatus.NOT_FOUND)
                .bodyText()
                .contains("NOT_FOUND");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateOnDuplicate() throws Exception {

        // given
        ItemUpdateRequest req = ItemUpdateRequest.builder()
                .id(2)
                .serialNo("A0004")
                .build();
        String body = mapper.writeValueAsString(req);

        ItemUpdateCommand shouldBePassed = req.toCommand();
        when(itemService.update(shouldBePassed))
                .thenThrow(new BusinessFlowException("from mock", CauseType.DUPLICATE));

        // when
        MvcTestResult result = mockMvc
                .put()
                .uri("/admin/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .exchange();

        // then
        assertThat(result)
                .hasStatus(HttpStatus.CONFLICT)
                .bodyText()
                .contains("DUPLICATE");
    }

    @Test
    void testUpdateOnAuthenticationError() throws Exception {

        // given
        ItemUpdateRequest req = ItemUpdateRequest.builder()
                .id(2)
                .serialNo("UPDATE-1")
                .itemName("UPDATE-2")
                .build();
        String body = mapper.writeValueAsString(req);

        // when
        MvcTestResult result = mockMvc
                .put()
                .uri("/admin/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .exchange();

        // then
        assertThat(result)
                .hasStatus(HttpStatus.UNAUTHORIZED);
        verify(itemService, never()).update(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDelete() throws Exception {

        // given
        int deleteId = 1;
        Mockito.doNothing().when(itemService).delete(new ItemId(deleteId));

        // when
        MvcTestResult result = mockMvc
                .delete()
                .uri("/admin/items/{id}", deleteId)
                .exchange();

        // then
        assertThat(result)
                .hasStatusOk();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteOnParameterError() throws Exception {

        // given
        int deleteId = -1;

        // when
        MvcTestResult result = mockMvc
                .delete()
                .uri("/admin/items/{id}", deleteId)
                .exchange();

        // then
        assertThat(result)
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyText()
                .contains("パラメーターエラーが発生しました", "id");
        verify(itemService, never()).delete(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteOnNotFound() throws Exception {

        // given
        int deleteId = 999;
        Mockito.doThrow(new BusinessFlowException("from mock", CauseType.NOT_FOUND))
                .when(itemService).delete(new ItemId(deleteId));

        // when
        MvcTestResult result = mockMvc
                .delete()
                .uri("/admin/items/{id}", deleteId)
                .exchange();

        // then
        assertThat(result)
                .hasStatus(HttpStatus.NOT_FOUND)
                .bodyText()
                .contains("NOT_FOUND");
    }

    @Test
    void testDeleteOnAuthenticationError() throws Exception {

        // given
        int deleteId = 1;

        // when
        MvcTestResult result = mockMvc
                .delete()
                .uri("/admin/items/{id}", deleteId)
                .exchange();

        // then
        assertThat(result)
                .hasStatus(HttpStatus.UNAUTHORIZED);
        verify(itemService, never()).delete(any());
    }
}