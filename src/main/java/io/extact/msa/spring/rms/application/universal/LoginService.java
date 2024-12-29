package io.extact.msa.spring.rms.application.universal;

import java.util.Optional;

import org.springframework.stereotype.Service;

import io.extact.msa.spring.rms.domain.user.UserRepository;
import io.extact.msa.spring.rms.domain.user.model.User;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final UserRepository repository;

    public Optional<User> login(String loginId, String password) {
        return repository.findByLoginIdAndPasswod(loginId, password);
    }
}
