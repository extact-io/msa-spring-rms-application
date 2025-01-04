package io.extact.msa.spring.rms.interfaces.console.screen.admin;

import static io.extact.msa.spring.rms.interfaces.console.common.ClientConstants.*;

import io.extact.msa.spring.platform.fw.exception.BusinessFlowException;
import io.extact.msa.spring.rms.application.admin.UserAddCommand;
import io.extact.msa.spring.rms.application.admin.UserAdminService;
import io.extact.msa.spring.rms.domain.user.model.UserReference;
import io.extact.msa.spring.rms.domain.user.model.UserType;
import io.extact.msa.spring.rms.interfaces.console.screen.RmsScreen;
import io.extact.msa.spring.rms.interfaces.console.screen.TransitionMap.Transition;
import io.extact.msa.spring.rms.interfaces.console.textio.RmsStringInputReader.PatternMessage;
import io.extact.msa.spring.rms.interfaces.console.textio.TextIoUtils;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AddUserScreen implements RmsScreen {

    private final UserAdminService service;

    @Override
    public Transition play(UserReference loginUser, boolean printHeader) {

        if (printHeader) {
            TextIoUtils.printScreenHeader(loginUser, "ユーザ登録画面");
        }

        // 入力インフォメーションの表示
        TextIoUtils.println(ENTRY_USER_INFORMATION);

        // ログインIDの入力
        var loginId = TextIoUtils.newStringInputReader()
                .withMinLength(5)
                .withMaxLength(15)
                .withExcludeCheckString(SCREEN_BREAK_KEY)
                .read("ログインID");
        if (TextIoUtils.isBreak(loginId)) {
            return Transition.ADMIN_MAIN;
        }

        // パスワードの入力
        String password = TextIoUtils.newStringInputReader()
                .withMinLength(5)
                .withMaxLength(15)
                .read("パスワード");

        // ユーザ名の入力
        String userName = TextIoUtils.newStringInputReader()
                .withMinLength(1)
                .read("ユーザ名");

        // 電話番号の入力
        String phoneNumber = TextIoUtils.newStringInputReader()
                .withMaxLength(14)
                .withPattern(PatternMessage.PHONE_NUMBER)
                .withDefaultValue("")
                .read("電話番号（省略可）");

        // 連絡先の入力
        String contact = TextIoUtils.newStringInputReader()
                .withMaxLength(15)
                .withDefaultValue("")
                .read("連絡先（省略可）");

        // 会員種別の入力
        UserType userType = TextIoUtils.newEnumInputReader(UserType.class)
                .withDefaultValue(UserType.MEMBER)
                .read("権限");

        UserAddCommand command = UserAddCommand.builder()
                .password(password)
                .userName(userName)
                .phoneNumber(phoneNumber)
                .contact(contact)
                .userType(userType)
                .build();

        // ユーザ登録の実行
        try {
            UserReference newUser = service.add(command);
            this.printResultInformation(newUser);
            return Transition.ADMIN_MAIN;

        } catch (BusinessFlowException e) {
            TextIoUtils.printServerError(e);
            return play(loginUser, false); // start over!!

        }
    }

    private void printResultInformation(UserReference newUser) {
        TextIoUtils.blankLine();
        TextIoUtils.println("***** ユーザ登録結果 *****");
        TextIoUtils.println("ユーザ番号：" + newUser.getId());
        TextIoUtils.println("ログインID：" + newUser.getLoginId());
        TextIoUtils.println("パスワード：" + newUser.getPassword());
        TextIoUtils.println("ユーザ名：" + newUser.getProfile().getUserName());
        TextIoUtils.println("電話番号：" + newUser.getProfile().getPhoneNumber());
        TextIoUtils.println("連絡先：" + newUser.getProfile().getContact());
        TextIoUtils.println("権限：" + newUser.getUserType().name());
        TextIoUtils.blankLine();
        TextIoUtils.waitPressEnter();
    }
}
