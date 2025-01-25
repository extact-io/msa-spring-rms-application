package io.extact.msa.spring.rms.interfaces.webapi.admin;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import io.extact.msa.spring.platform.fw.domain.constraint.RmsId;
import io.extact.msa.spring.platform.fw.web.RmsRestController;
import io.extact.msa.spring.rms.application.admin.ItemAdminService;
import io.extact.msa.spring.rms.domain.item.model.ItemId;
import lombok.RequiredArgsConstructor;

@RmsRestController("/items")
@RequiredArgsConstructor
public class ItemAdminController {

    private final ItemAdminService service;

    @GetMapping
    public List<ItemAdminResponse> getAll() {
        return service
                .getAll()
                .stream()
                .map(ItemAdminResponse::from)
                .toList();
    }

    @PostMapping
    public ItemAdminResponse add(@Valid @RequestBody ItemAddRequest request) {
        return service
                .add(request.toCommand())
                .transform(ItemAdminResponse::from);
    }

    @PutMapping
    public ItemAdminResponse update(@Valid @RequestBody ItemUpdateRequest request) {
        return service
                .update(request.toCommand())
                .transform(ItemAdminResponse::from);
    }

    @DeleteMapping("/{id}")
    public void delete(@RmsId @PathVariable("id") Integer itemId) {
        service.delete(new ItemId(itemId));
    }
}
