package io.extact.msa.spring.rms.webapi.universal;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import io.extact.msa.spring.platform.core.jwt.encode.GenerateToken;
import io.extact.msa.spring.platform.fw.interfaces.webapi.ApiController;
import io.extact.msa.spring.rms.application.universal.LoginService;
import io.extact.msa.spring.rms.domain.user.constraint.LoginId;
import io.extact.msa.spring.rms.domain.user.constraint.Passowrd;
import lombok.RequiredArgsConstructor;

@ApiController("/login")
@RequiredArgsConstructor
public class LoginController {

    private final LoginService service;

    @GetMapping
    @GenerateToken
    public LoginUserResponse login(
            @LoginId @RequestParam String loginId,
            @Passowrd @RequestParam String password) {
        return service
                .login(loginId, password)
                .transform(LoginUserResponse::from);

    }

    @PostMapping
    @GenerateToken
    public LoginUserResponse login(@Valid @RequestBody LoginRequest request) {
        return service
                .login(request.loginId(), request.password())
                .transform(LoginUserResponse::from);
    }
}
