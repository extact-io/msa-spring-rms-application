package io.extact.msa.spring.rms.interfaces.webapi.universal;

import io.extact.msa.spring.platform.fw.domain.constraint.LoginId;
import io.extact.msa.spring.platform.fw.domain.constraint.Passowrd;
import lombok.Builder;

@Builder
record LoginRequest(
        @LoginId String loginId,
        @Passowrd String password) {
}
