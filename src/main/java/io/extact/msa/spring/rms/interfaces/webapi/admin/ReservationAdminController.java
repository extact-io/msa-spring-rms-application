package io.extact.msa.spring.rms.interfaces.webapi.admin;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import io.extact.msa.spring.platform.fw.domain.constraint.RmsId;
import io.extact.msa.spring.platform.fw.web.RmsRestController;
import io.extact.msa.spring.rms.application.admin.ReservationAdminService;
import io.extact.msa.spring.rms.domain.reservation.model.ReservationId;
import lombok.RequiredArgsConstructor;

@RmsRestController("/reservations")
@RequiredArgsConstructor
public class ReservationAdminController {

    private final ReservationAdminService service;

    @GetMapping
    public List<ReservationAdminResponse> getAll() {
        return service
                .getAll()
                .stream()
                .map(ReservationAdminResponse::from)
                .toList();
    }

    @PutMapping
    public ReservationAdminResponse update(@Valid @RequestBody ReservationUpdateRequest request) {
        return service
                .update(request.toCommand())
                .transform(ReservationAdminResponse::from);
    }

    @DeleteMapping("/{id}")
    public void delete(@RmsId @PathVariable("id") Integer itemId) {
        service.delete(new ReservationId(itemId));
    }
}
