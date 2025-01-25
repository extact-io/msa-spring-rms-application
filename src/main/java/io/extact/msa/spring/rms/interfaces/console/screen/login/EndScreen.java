package io.extact.msa.spring.rms.interfaces.console.screen.login;

import io.extact.msa.spring.rms.domain.user.model.UserReference;
import io.extact.msa.spring.rms.interfaces.console.screen.RmsScreen;
import io.extact.msa.spring.rms.interfaces.console.screen.TransitionMap.Transition;

public class EndScreen implements RmsScreen {

    @Override
    public Transition play(UserReference loginUser, boolean printHeader) {
        return null;
    }
}
