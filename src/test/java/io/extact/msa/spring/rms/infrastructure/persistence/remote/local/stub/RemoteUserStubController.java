package io.extact.msa.spring.rms.infrastructure.persistence.remote.local.stub;

import static io.extact.msa.spring.rms.PersistedTestData.*;

import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.annotation.PostConstruct;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.extact.msa.spring.platform.fw.interfaces.webapi.RmsRestController;
import io.extact.msa.spring.rms.domain.user.model.User;
import io.extact.msa.spring.rms.infrastructure.persistence.remote.user.RemoteUser;

@RmsRestController("/users")
public class RemoteUserStubController extends RemoteStubController<User, RemoteUser> {

    private Map<Integer, RemoteUser> usersMap;

    @PostConstruct
    void init() {
        usersMap = new LinkedHashMap<>();
        usersMap.put(user1.getId().id(), RemoteUser.from(user1));
        usersMap.put(user2.getId().id(), RemoteUser.from(user2));
        usersMap.put(user3.getId().id(), RemoteUser.from(user3));
    }

    @GetMapping("/unique")
    public RemoteUser findByLoginId(@RequestParam("login-id") String loginId) {
        return usersMap.values().stream()
                .filter(user -> user.loginId().equals(loginId))
                .findAny()
                .orElse(null);
    }

    @GetMapping("/auth")
    public RemoteUser findByLoginIdAndPassword(
            @RequestParam("login-id") String loginId,
            @RequestParam String password) {
        
        return usersMap.values().stream()
                .filter(user -> user.loginId().equals(loginId))
                .filter(user -> user.password().equals(password))
                .findAny()
                .orElse(null);
    }

    @Override
    public Map<Integer, RemoteUser> entityMap() {
        return usersMap;
    }
}
