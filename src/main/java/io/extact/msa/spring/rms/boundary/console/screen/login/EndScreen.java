package io.extact.msa.spring.rms.boundary.console.screen.login;

import io.extact.msa.spring.rms.boundary.console.screen.RmsScreen;
import io.extact.msa.spring.rms.boundary.console.screen.TransitionMap.Transition;
import io.extact.msa.spring.rms.domain.user.model.UserReference;

public class EndScreen implements RmsScreen {

    @Override
    public Transition play(UserReference loginUser, boolean printHeader) {
        return null;
    }
}
