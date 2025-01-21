package io.extact.msa.spring.rms.boundary.console.screen;

import io.extact.msa.spring.rms.boundary.console.screen.TransitionMap.Transition;
import io.extact.msa.spring.rms.domain.user.model.UserReference;

public interface RmsScreen {
    Transition play(UserReference loginUser, boolean printHeader);
}