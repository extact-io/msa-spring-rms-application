package io.extact.msa.spring.rms.application.universal;

import org.springframework.transaction.annotation.Transactional;

import io.extact.msa.spring.platform.fw.exception.BusinessFlowException;
import io.extact.msa.spring.platform.fw.exception.BusinessFlowException.CauseType;
import io.extact.msa.spring.rms.domain.user.UserRepository;
import io.extact.msa.spring.rms.domain.user.model.UserReference;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Transactional
public class LoginService {

    private final UserRepository repository;

    public UserReference login(String loginId, String password) {
        return repository
                .findByLoginIdAndPasswod(loginId, password)
                .orElseThrow(() -> new BusinessFlowException("loginId or password is different", CauseType.NOT_FOUND));
    }
}
