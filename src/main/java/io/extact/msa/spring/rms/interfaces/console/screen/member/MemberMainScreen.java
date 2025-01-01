package io.extact.msa.spring.rms.interfaces.console.screen.member;

import io.extact.msa.spring.rms.domain.user.model.UserReference;
import io.extact.msa.spring.rms.interfaces.console.screen.RmsScreen;
import io.extact.msa.spring.rms.interfaces.console.screen.TransitionMap.Transition;
import io.extact.msa.spring.rms.interfaces.console.textio.TextIoUtils;
import lombok.RequiredArgsConstructor;

public class MemberMainScreen implements RmsScreen {

    @RequiredArgsConstructor
    public enum MemberMenuList {

        INQUIRY("予約照会", Transition.INQUIRY_RESERVATION),
        ENTRY("レンタル品予約", Transition.ENTRY_RESERVATRION),
        CANCEL("予約キャンセル", Transition.CANCEL_RESERVATRION),
        RELOGIN("再ログイン", Transition.LOGIN),
        END("終了", Transition.END);

        private final String name;
        private final Transition transition;

        Transition getTransition() {
            return transition;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    @Override
    public Transition play(UserReference loginUser, boolean printHeader) {

        TextIoUtils.printScreenHeader(loginUser, "レンタル会員サービスメニュー画面");

        MemberMenuList selectedMenu = TextIoUtils.newEnumInputReader(MemberMenuList.class)
                .read("メニュー番号を入力して下さい。");

        return selectedMenu.getTransition();
    }

}
