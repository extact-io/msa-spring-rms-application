package io.extact.msa.spring.rms.interfaces.console.screen;

import io.extact.msa.spring.rms.domain.user.model.UserModelView;
import io.extact.msa.spring.rms.interfaces.console.screen.TransitionMap.Transition;

public interface RmsScreen {
    Transition play(UserModelView loginUser, boolean printHeader);
}