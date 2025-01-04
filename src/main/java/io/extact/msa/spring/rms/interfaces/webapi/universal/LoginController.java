package io.extact.msa.spring.rms.interfaces.webapi.universal;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import io.extact.msa.spring.platform.fw.web.RmsRestController;
import io.extact.msa.spring.rms.application.universal.LoginService;
import io.extact.msa.spring.rms.domain.user.constraints.LoginId;
import io.extact.msa.spring.rms.domain.user.constraints.Passowrd;
import lombok.RequiredArgsConstructor;

@RmsRestController("/login")
@RequiredArgsConstructor
public class LoginController {

    private final LoginService service;

    @GetMapping
    public LoginUserResponse login(
            @LoginId @RequestParam("loginId") String loginId,
            @Passowrd @RequestParam("password") String password) {
        return service
                .login(loginId, password)
                .transform(LoginUserResponse::from);

    }

    @PostMapping
    public LoginUserResponse login(@Valid @RequestBody LoginRequest request) {
        return service
                .login(request.loginId(), request.password())
                .transform(LoginUserResponse::from);
    }
}
