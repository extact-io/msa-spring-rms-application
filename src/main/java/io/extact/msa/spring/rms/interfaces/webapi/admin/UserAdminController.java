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
import io.extact.msa.spring.rms.application.admin.UserAdminService;
import io.extact.msa.spring.rms.domain.user.model.UserId;
import lombok.RequiredArgsConstructor;

@RmsRestController("/users")
@RequiredArgsConstructor
public class UserAdminController {

    private final UserAdminService service;

    @GetMapping
    public List<UserAdminResponse> getAll() {
        return service
                .getAll()
                .stream()
                .map(UserAdminResponse::from)
                .toList();
    }

    @PostMapping
    public UserAdminResponse add(@Valid @RequestBody UserAddRequest request) {
        return service
                .add(request.toCommand())
                .transform(UserAdminResponse::from);
    }

    @PutMapping
    public UserAdminResponse update(@Valid @RequestBody UserUpdateRequest request) {
        return service
                .update(request.toCommand())
                .transform(UserAdminResponse::from);
    }

    @DeleteMapping("/{id}")
    public void delete(@RmsId @PathVariable("id") Integer itemId) {
        service.delete(new UserId(itemId));
    }
}
