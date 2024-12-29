package io.extact.msa.spring.rms.domain.user.model;

import io.extact.msa.spring.platform.fw.domain.constraint.RmsId;
import io.extact.msa.spring.platform.fw.domain.model.Identity;

public record UserId(
        @RmsId int id) implements Identity {
}
