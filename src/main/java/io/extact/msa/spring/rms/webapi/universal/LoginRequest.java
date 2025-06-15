package io.extact.msa.spring.rms.webapi.universal;

import io.extact.msa.spring.rms.domain.user.constraint.LoginId;
import io.extact.msa.spring.rms.domain.user.constraint.Passowrd;
import lombok.Builder;

@Builder
record LoginRequest(
        @LoginId String loginId,
        @Passowrd String password) {
}
