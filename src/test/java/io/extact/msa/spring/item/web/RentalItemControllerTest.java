package io.extact.msa.spring.item.web;

import static org.hamcrest.CoreMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.extact.msa.spring.item.application.EditRentalItemCommand;
import io.extact.msa.spring.item.application.RegisterRentalItemCommand;
import io.extact.msa.spring.item.application.RentalItemApplicationService;
import io.extact.msa.spring.item.domain.model.ItemId;
import io.extact.msa.spring.item.domain.model.RentalItem;
import io.extact.msa.spring.platform.fw.exception.BusinessFlowException;
import io.extact.msa.spring.platform.fw.exception.BusinessFlowException.CauseType;

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
@WebMvcTest(LoginController.class)
@Import(WebConfig.class)
class RentalItemControllerTest {

    private static final Item item1 = Item.reconstruct(1, "A0001", "レンタル品1号");
    private static final Item item2 = Item.reconstruct(2, "A0002", "レンタル品2号");
    private static final Item item3 = Item.reconstruct(3, "A0003", "レンタル品3号");
    private static final Item item4 = Item.reconstruct(4, "A0004", "レンタル品4号");

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper mapper;
    @MockBean
    private RentalItemApplicationService itemService;

    @Test
    @WithMockUser
    void testGetAll() throws Exception {

        // given
        when(itemService.getAll())
                .thenReturn(List.of(item1, item2, item3, item4));
        // when
        mockMvc.perform(get("/items"))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].serialNo").value("A0001"))
                .andExpect(jsonPath("$[0].itemName").value("レンタル品1号")); // 2件目以降の確認は省略
    }

    @Test
    @WithMockUser
    void testGetAllReturnEmpty() throws Exception {

        // given
        when(itemService.getAll())
                .thenReturn(List.of());
        // when
        mockMvc.perform(get("/items"))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testGetAllOnAuthenticationError() throws Exception {

        // given
        // @WithMockUserなし

        // when
        mockMvc.perform(get("/items"))
                // then
                .andExpect(status().isUnauthorized());

        // then
        verify(itemService, never()).getAll();
    }

    @Test
    @WithMockUser
    void testGetOne() throws Exception {

        // given
        when(itemService.getById(new ItemId(2)))
                .thenReturn(Optional.of(item2));
        // when
        mockMvc.perform(get("/items/{id}", 2))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.serialNo").value("A0002"))
                .andExpect(jsonPath("$.itemName").value("レンタル品2号"));
    }

    @Test
    @WithMockUser
    void testGetOneOnNotFound() throws Exception {

        // given
        when(itemService.getById(new ItemId(9)))
                .thenReturn(Optional.empty());
        // when
        mockMvc.perform(get("/items/{id}", 9))
                // then
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        // then
        verify(itemService, atLeastOnce()).getById(new ItemId(9));
    }

    @Test
    void testGetOneOnAuthenticationError() throws Exception {

        // given
        // @WithMockUserなし

        // when
        mockMvc.perform(get("/items/{id}", 1))
                // then
                .andExpect(status().isUnauthorized());

        // then
        verify(itemService, never()).getById(any());
    }

    @Test
    @WithMockUser
    void testGetOneOnParameterError() throws Exception {

        // given
        int errorParm = -1;

        // when
        mockMvc.perform(get("/items/{id}", errorParm))
                // then
                .andDo(result -> result.getResponse().setCharacterEncoding("UTF-8"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("パラメーターエラーが発生しました")));

        // then
        verify(itemService, never()).getById(any());
    }

    @Test
    @WithMockUser
    void testAdd() throws Exception {

        // given
        ItemAddRequest req = ItemAddRequest.builder()
                .serialNo("newNo")
                .itemName("追加アイテム")
                .build();
        String body = mapper.writeValueAsString(req);

        ItemAddCommand shouldBePassed = req.transform(RequestUtils::toRegisterCommand);
        when(itemService.register(shouldBePassed))
                .thenReturn(Item.reconstruct(5, req.serialNo(), req.itemName()));

        // when
        mockMvc.perform(post("/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.serialNo").value("newNo"))
                .andExpect(jsonPath("$.itemName").value("追加アイテム"));
    }

    @Test
    @WithMockUser
    void testAddOnParameterError() throws Exception {

        // given
        ItemAddRequest request = ItemAddRequest.builder()
                .build(); // empty value
        String body = mapper.writeValueAsString(request);

        // when
        mockMvc.perform(post("/items")
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
        verify(itemService, never()).register(any());
    }

    @Test
    @WithMockUser
    void testAddOnDuplicate() throws Exception {

        // given
        ItemAddRequest req = ItemAddRequest.builder()
                .serialNo("A0004")
                .itemName("レンタル品5号")
                .build();
        String body = mapper.writeValueAsString(req);

        ItemAddCommand shouldBePassed = req.transform(RequestUtils::toRegisterCommand);
        when(itemService.register(shouldBePassed))
                .thenThrow(new BusinessFlowException("from mock", CauseType.DUPLICATE));

        // when
        mockMvc.perform(post("/items")
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
        mockMvc.perform(post("/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                // then
                .andDo(result -> result.getResponse().setCharacterEncoding("UTF-8"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void testUpdate() throws Exception {

        // given
        ItemUpdateRequest req = ItemUpdateRequest.builder()
                .id(2)
                .serialNo("UPDATE-1")
                .itemName("UPDATE-2")
                .build();
        String body = mapper.writeValueAsString(req);

        ItemUpdateCommand shouldBePassed = req.transform(RequestUtils::toEditCommand);
        when(itemService.edit(shouldBePassed))
                .thenReturn(Item.reconstruct(5, req.serialNo(), req.itemName()));

        // when
        mockMvc.perform(put("/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.serialNo").value("UPDATE-1"))
                .andExpect(jsonPath("$.itemName").value("UPDATE-2"));
    }

    @Test
    @WithMockUser
    void testUpdateOnParameterError() throws Exception {

        // given
        ItemUpdateRequest req = ItemUpdateRequest.builder()
                .serialNo("@@@@@") // 使用不可文字
                .itemName("1234567890123456") // 桁数オーバー
                .build();
        String body = mapper.writeValueAsString(req);

        // when
        mockMvc.perform(put("/items")
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
        verify(itemService, never()).register(any());
    }

    @Test
    @WithMockUser
    void testUpdateOnNotFound() throws Exception {

        // given
        ItemUpdateRequest req = ItemUpdateRequest.builder()
                .id(9) // not exist id
                .serialNo("UPDATE-1")
                .itemName("UPDATE-2")
                .build();
        String body = mapper.writeValueAsString(req);

        ItemUpdateCommand shouldBePassed = req.transform(RequestUtils::toEditCommand);
        when(itemService.edit(shouldBePassed))
                .thenThrow(new BusinessFlowException("from mock", CauseType.NOT_FOUND));

        mockMvc.perform(put("/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                // then
                .andDo(result -> result.getResponse().setCharacterEncoding("UTF-8"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("NOT_FOUND")));

        // then
        verify(itemService, never()).register(any());
    }

    @Test
    @WithMockUser
    void testUpdateOnDuplicate() throws Exception {

        // given
        ItemUpdateRequest req = ItemUpdateRequest.builder()
                .id(2)
                .serialNo("A0004")
                .build();
        String body = mapper.writeValueAsString(req);

        ItemUpdateCommand shouldBePassed = req.transform(RequestUtils::toEditCommand);
        when(itemService.edit(shouldBePassed))
                .thenThrow(new BusinessFlowException("from mock", CauseType.DUPLICATE));

        mockMvc.perform(put("/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                // then
                .andDo(result -> result.getResponse().setCharacterEncoding("UTF-8"))
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("DUPLICATE")));

        // then
        verify(itemService, never()).register(any());
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

        mockMvc.perform(put("/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                // then
                .andDo(result -> result.getResponse().setCharacterEncoding("UTF-8"))
                .andExpect(status().isUnauthorized());

        // then
        verify(itemService, never()).register(any());
    }

    @Test
    @WithMockUser
    void testDelete() throws Exception {

        // given
        int deleteId = 1;
        Mockito.doNothing().when(itemService).delete(new ItemId(deleteId));

        // when
        mockMvc.perform(delete("/items/{id}", deleteId))
                // then
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testDeleteOnParameterError() throws Exception {

        // given
        int deleteId = -1;

        // when
        mockMvc.perform(delete("/items/{id}", deleteId))
                // then
                .andDo(result -> result.getResponse().setCharacterEncoding("UTF-8"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(allOf(
                        containsString("パラメーターエラーが発生しました"), //
                        containsString("id"))));

        // then
        verify(itemService, never()).getById(any());
   }

    @Test
    @WithMockUser
    void testDeleteOnNotFound() throws Exception {

        // given
        int deleteId = 999;
        Mockito.doThrow(new BusinessFlowException("from mock", CauseType.NOT_FOUND))
                .when(itemService).delete(new ItemId(deleteId));

        // when
        mockMvc.perform(delete("/items/{id}", deleteId))
                // then
                .andDo(result -> result.getResponse().setCharacterEncoding("UTF-8"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("NOT_FOUND")));

        // then
        verify(itemService, never()).getById(any());
   }

    @Test
    void testDeleteOnAuthenticationError() throws Exception {

        // given
        int deleteId = 1;

        // when
        mockMvc.perform(delete("/items/{id}", deleteId))
                // then
                .andDo(result -> result.getResponse().setCharacterEncoding("UTF-8"))
                .andExpect(status().isUnauthorized());

        // then
        verify(itemService, never()).getById(any());
   }

    @Test
    @WithMockUser
    void testExist() throws Exception {

        // given
        int existId = 1;
        when(itemService.getById(new ItemId(existId)))
                .thenReturn(Optional.of(item1));

        // when
        mockMvc.perform(get("/items/exists/{id}", existId))
                // then
            .andExpect(status().isOk())
            .andExpect(content().string("true"));
    }

    @Test
    void testExistOnAuthenticationError() throws Exception {

        // given
        int existId = 1;

        // when
        mockMvc.perform(delete("/items/exists/{id}", existId))
                // then
                .andDo(result -> result.getResponse().setCharacterEncoding("UTF-8"))
                .andExpect(status().isUnauthorized());

        // then
        verify(itemService, never()).getById(any());
   }

}