package io.extact.msa.spring.rms.interfaces.webapi.universal;

import io.extact.msa.spring.rms.domain.user.constraints.LoginId;
import io.extact.msa.spring.rms.domain.user.constraints.Passowrd;
import lombok.Builder;

@Builder
record LoginRequest(
        @LoginId String loginId,
        @Passowrd String password) {
}
