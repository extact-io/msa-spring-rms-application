package io.extact.msa.spring.rms.interfaces.webapi.universal;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import io.extact.msa.spring.platform.fw.web.RmsRestController;
import io.extact.msa.spring.rms.application.universal.UserProfileService;
import lombok.RequiredArgsConstructor;

@RmsRestController("/profiles")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService service;

    @GetMapping("/own")
    public UserProfileResponse getOwnProfile() {
        return service
                .getOwnProfile()
                .transform(UserProfileResponse::from);
    }

    @PostMapping("/own")
    public UserProfileResponse updateOwnProfile(@Valid @RequestBody UserProfileUpdateRequest request) {
        return service
                .updateOwnProfile(request.toCommand())
                .transform(UserProfileResponse::from);
    }
}
