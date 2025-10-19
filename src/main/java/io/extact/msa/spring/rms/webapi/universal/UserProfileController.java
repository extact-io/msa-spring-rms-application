package io.extact.msa.spring.rms.webapi.universal;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import io.extact.msa.spring.platform.fw.interfaces.webapi.ApiController;
import io.extact.msa.spring.rms.application.universal.UserProfileService;
import lombok.RequiredArgsConstructor;

@ApiController("/profiles")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService service;

    @GetMapping("/own")
    public UserProfileResponse getOwnProfile() {
        return service
                .getOwnProfile()
                .transform(UserProfileResponse::from);
    }

    @PutMapping("/own")
    public UserProfileResponse updateOwnProfile(@Valid @RequestBody UserProfileUpdateRequest request) {
        return service
                .updateOwnProfile(request.toCommand())
                .transform(UserProfileResponse::from);
    }
}
