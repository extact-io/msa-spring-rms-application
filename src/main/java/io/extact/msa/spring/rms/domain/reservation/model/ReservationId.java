package io.extact.msa.spring.rms.domain.reservation.model;

import io.extact.msa.spring.platform.fw.domain.constraint.RmsId;
import io.extact.msa.spring.platform.fw.domain.model.Identity;

public record ReservationId(
        @RmsId int id) implements Identity {
}
