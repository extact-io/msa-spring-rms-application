package io.extact.msa.spring.rms.application.universal;

import io.extact.msa.spring.platform.core.transaction.ReadOnly;
import io.extact.msa.spring.platform.fw.application.ApplicationService;
import io.extact.msa.spring.rms.domain.user.UserRepository;
import io.extact.msa.spring.rms.domain.user.model.UserModelView;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@ApplicationService
public class LoginService {

    private final UserRepository repository;

    @ReadOnly
    public UserModelView login(String loginId, String password) {
        return repository
                .findByLoginIdAndPassword(loginId, password)
                .orElseThrow(() -> new LoginFailedException("loginId or password is different"));
    }
}
