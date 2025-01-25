package io.extact.msa.spring.rms.interfaces.console.screen;

import java.util.EnumMap;
import java.util.Map;

import io.extact.msa.spring.rms.interfaces.console.screen.login.LoginScreen;

public class TransitionMap {

    private Map<Transition, RmsScreen> transitionMap = new EnumMap<>(Transition.class);

    public LoginScreen stratScreen() {
        return (LoginScreen) transitionMap.get(Transition.LOGIN);
    }

    public void add(Transition name, RmsScreen screen) {
        transitionMap.put(name, screen);
    }

    public RmsScreen nextScreen(Transition name) {
        return transitionMap.get(name);
    }

    public enum Transition {
        LOGIN,
        MEMBER_MAIN,
        INQUIRY_RESERVATION,
        ENTRY_RESERVATRION,
        CANCEL_RESERVATRION,
        ADMIN_MAIN,
        ENTRY_RENTAL_ITEM,
        ENTRY_USER,
        EDIT_USER,
        END
    }
}
