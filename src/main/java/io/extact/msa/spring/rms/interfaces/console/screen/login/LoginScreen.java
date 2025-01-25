package io.extact.msa.spring.rms.interfaces.console.screen.login;

import static io.extact.msa.spring.rms.interfaces.console.common.ClientConstants.*;

import io.extact.msa.spring.platform.core.env.MainModuleInformation;
import io.extact.msa.spring.platform.fw.exception.BusinessFlowException;
import io.extact.msa.spring.rms.application.universal.LoginService;
import io.extact.msa.spring.rms.domain.user.model.UserReference;
import io.extact.msa.spring.rms.interfaces.console.screen.RmsScreen;
import io.extact.msa.spring.rms.interfaces.console.screen.TransitionMap.Transition;
import io.extact.msa.spring.rms.interfaces.console.textio.TextIoUtils;
import lombok.RequiredArgsConstructor;

/**
 * アプリケーション開始画面。
 * 開始処理としてのログインを担う
 */
@RequiredArgsConstructor
public class LoginScreen implements RmsScreen {

    private final LoginService service;
    private final LoginEventObserver loginObserver;
    private final MainModuleInformation moduleInfo;

    @Override
    public Transition play(UserReference dummy, boolean printHeader) {
        try {
            if (printHeader) {
                // 認証画面のヘッダーを表示する
                TextIoUtils.println("[version:" + moduleInfo.version() + "/build-time:" + moduleInfo.buildTIme() + "]");
                TextIoUtils.println(LOGIN_INFORMATION);
                TextIoUtils.blankLine();
            }

            String loginId = TextIoUtils.newStringInputReader()
                    .withMinLength(5)
                    .withMaxLength(10)
                    .withDefaultValue("edamame")
                    .read("ログインID");
            if (loginId.equals(SCREEN_BREAK_KEY)) {
                return Transition.END;
            }

            String password = TextIoUtils.newStringInputReader()
                    .withMinLength(5)
                    .withMaxLength(10)
                    .withDefaultValue("edamame")
                    .withInputMasking(true)
                    .read("パスワード");

            // ログイン実行
            UserReference nowLoginUser = service.login(loginId, password);
            // 成功したのでログインユーザを通知
            loginObserver.onEvent(nowLoginUser);

            return nowLoginUser.getUserType().isAdmin() ? Transition.ADMIN_MAIN : Transition.MEMBER_MAIN;

        } catch (BusinessFlowException e) {
            TextIoUtils.printServerError(e);
            return play(dummy, false);

        }
    }
}
