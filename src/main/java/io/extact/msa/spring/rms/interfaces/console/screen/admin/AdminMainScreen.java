package io.extact.msa.spring.rms.interfaces.console.screen.admin;


import io.extact.msa.spring.rms.domain.user.model.UserReference;
import io.extact.msa.spring.rms.interfaces.console.screen.RmsScreen;
import io.extact.msa.spring.rms.interfaces.console.screen.TransitionMap.Transition;
import io.extact.msa.spring.rms.interfaces.console.textio.TextIoUtils;
import lombok.RequiredArgsConstructor;

public class AdminMainScreen implements RmsScreen {

    @RequiredArgsConstructor
    public enum AdminMenuList {

        ENTRY_RENTAL_ITEM("レンタル品登録", Transition.ENTRY_RENTAL_ITEM),
        ENTRY_USER("ユーザ登録", Transition.ENTRY_USER),
        EDIT_USER("ユーザ編集", Transition.EDIT_USER),
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

        TextIoUtils.printScreenHeader(loginUser, "管理者サービスメニュー画面");

        AdminMenuList selectedMenu = TextIoUtils
                .newEnumInputReader(AdminMenuList.class)
                .read("メニュー番号を入力して下さい。");

        return selectedMenu.getTransition();
    }
}
