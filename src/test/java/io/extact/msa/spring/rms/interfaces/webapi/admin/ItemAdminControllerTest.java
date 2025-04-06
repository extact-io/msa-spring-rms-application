package io.extact.msa.spring.rms.interfaces.webapi.admin;

import static io.extact.msa.spring.rms.PersistedTestData.*;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.extact.msa.spring.platform.core.env.EnvConfig;
import io.extact.msa.spring.platform.fw.exception.BusinessFlowException;
import io.extact.msa.spring.platform.fw.exception.BusinessFlowException.CauseType;
import io.extact.msa.spring.platform.fw.interfaces.webapi.RestControllerConfig;
import io.extact.msa.spring.rms.application.admin.ItemAddCommand;
import io.extact.msa.spring.rms.application.admin.ItemAdminService;
import io.extact.msa.spring.rms.application.admin.ItemUpdateCommand;
import io.extact.msa.spring.rms.domain.item.model.Item.ItemCreatable;
import io.extact.msa.spring.rms.domain.item.model.ItemId;
import io.extact.msa.spring.rms.interfaces.webapi.WebSecurityConfig;

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
@WebMvcTest(ItemAdminController.class)
@ActiveProfiles("test")
class ItemAdminControllerTest {

    private static final ItemCreatable testCreator = new ItemCreatable() {};

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper mapper;
    @MockitoBean
    private ItemAdminService itemService;

    @Configuration(proxyBeanMethods = false)
    @Import({
            EnvConfig.class,
            RestControllerConfig.class,
            WebSecurityConfig.class})
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
        mockMvc.perform(get("/admin/items"))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0].id").value(item1.getId().id()))
                .andExpect(jsonPath("$[0].serialNo").value(item1.getSerialNo()))
                .andExpect(jsonPath("$[0].itemName").value(item1.getItemName())); // 2件目以降の確認は省略
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllReturnEmpty() throws Exception {

        // given
        when(itemService.getAll())
                .thenReturn(List.of());
        // when
        mockMvc.perform(get("/admin/items"))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testGetAllOnAuthenticationError() throws Exception {

        // given
        // @WithMockUserなし

        // when
        mockMvc.perform(get("/admin/items"))
                // then
                .andExpect(status().isUnauthorized());

        // then
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
        mockMvc.perform(post("/admin/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.serialNo").value("newNo"))
                .andExpect(jsonPath("$.itemName").value("追加アイテム"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testAddOnParameterError() throws Exception {

        // given
        ItemAddRequest request = ItemAddRequest.builder()
                .build(); // empty value
        String body = mapper.writeValueAsString(request);

        // when
        mockMvc.perform(post("/admin/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                // then
                .andDo(result -> result.getResponse().setCharacterEncoding("UTF-8"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(allOf(
                        containsString("パラメーターエラーが発生しました"),
                        containsString("serialNo") //
                )));

        // then
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
        mockMvc.perform(post("/admin/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                // then
                .andDo(result -> result.getResponse().setCharacterEncoding("UTF-8"))
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("DUPLICATE")));
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
        mockMvc.perform(post("/admin/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                // then
                .andDo(result -> result.getResponse().setCharacterEncoding("UTF-8"))
                .andExpect(status().isUnauthorized());

        // then
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
        mockMvc.perform(put("/admin/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.serialNo").value("UPDATE-1"))
                .andExpect(jsonPath("$.itemName").value("UPDATE-2"));
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
        mockMvc.perform(put("/admin/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                // then
                .andDo(result -> result.getResponse().setCharacterEncoding("UTF-8"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(allOf(
                        containsString("パラメーターエラーが発生しました"),
                        containsString("id"), //
                        containsString("serialNo"), //
                        containsString("itemName"))));

        // then
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

        mockMvc.perform(put("/admin/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                // then
                .andDo(result -> result.getResponse().setCharacterEncoding("UTF-8"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("NOT_FOUND")));
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

        mockMvc.perform(put("/admin/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                // then
                .andDo(result -> result.getResponse().setCharacterEncoding("UTF-8"))
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("DUPLICATE")));
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

        mockMvc.perform(put("/admin/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                // then
                .andDo(result -> result.getResponse().setCharacterEncoding("UTF-8"))
                .andExpect(status().isUnauthorized());

        // then
        verify(itemService, never()).update(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDelete() throws Exception {

        // given
        int deleteId = 1;
        Mockito.doNothing().when(itemService).delete(new ItemId(deleteId));

        // when
        mockMvc.perform(delete("/admin/items/{id}", deleteId))
                // then
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteOnParameterError() throws Exception {

        // given
        int deleteId = -1;

        // when
        mockMvc.perform(delete("/admin/items/{id}", deleteId))
                // then
                .andDo(result -> result.getResponse().setCharacterEncoding("UTF-8"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(allOf(
                        containsString("パラメーターエラーが発生しました"), //
                        containsString("id"))));

        // then
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
        mockMvc.perform(delete("/admin/items/{id}", deleteId))
                // then
                .andDo(result -> result.getResponse().setCharacterEncoding("UTF-8"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("NOT_FOUND")));
   }

    @Test
    void testDeleteOnAuthenticationError() throws Exception {

        // given
        int deleteId = 1;

        // when
        mockMvc.perform(delete("/admin/items/{id}", deleteId))
                // then
                .andDo(result -> result.getResponse().setCharacterEncoding("UTF-8"))
                .andExpect(status().isUnauthorized());

        // then
        verify(itemService, never()).delete(any());
   }
}