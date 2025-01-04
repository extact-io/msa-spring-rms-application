package io.extact.msa.spring.rms.interfaces.console.screen.admin;

import static io.extact.msa.spring.rms.interfaces.console.common.ClientConstants.*;

import java.util.List;

import io.extact.msa.spring.platform.fw.exception.BusinessFlowException;
import io.extact.msa.spring.rms.application.admin.UserAdminService;
import io.extact.msa.spring.rms.application.admin.UserUpdateCommand;
import io.extact.msa.spring.rms.domain.user.model.UserReference;
import io.extact.msa.spring.rms.domain.user.model.UserType;
import io.extact.msa.spring.rms.interfaces.console.screen.RmsScreen;
import io.extact.msa.spring.rms.interfaces.console.screen.TransitionMap.Transition;
import io.extact.msa.spring.rms.interfaces.console.textio.RmsStringInputReader.PatternMessage;
import io.extact.msa.spring.rms.interfaces.console.textio.TextIoUtils;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UpdateUserScreen implements RmsScreen {

    private final UserAdminService service;

    @Override
    public Transition play(UserReference loginUser, boolean printHeader) {

        if (printHeader) {
            TextIoUtils.printScreenHeader(loginUser, "ユーザ情報編集画面");
        }

        // ユーザ一覧を表示
        List<? extends UserReference> users = service.getAll();
        TextIoUtils.println(EDIT_USER_INFORMATION);
        users.forEach(dto -> TextIoUtils.println(USER_FORMAT.format(dto)));
        TextIoUtils.blankLine();

        // 編集するユーザを選択
        int selectId = TextIoUtils.newIntInputReader()
                .withSelectableValues(users
                        .stream()
                        .map(user -> user.getId().id())
                        .toList(),
                        SCREEN_BREAK_VALUE)
                .read("ユーザ番号");
        if (TextIoUtils.isBreak(selectId)) {
            return Transition.ADMIN_MAIN;
        }
        TextIoUtils.blankLine();

        UserReference targetUser = users.stream()
                .filter(user -> user.getId().id() == selectId)
                .findFirst()
                .get();

        // パスワードの入力
        String password = TextIoUtils.newStringInputReader()
                .withDefaultValue(targetUser.getPassword())
                .withMinLength(5)
                .withMaxLength(15)
                .read("パスワード");

        // ユーザ名の入力
        String userName = TextIoUtils.newStringInputReader()
                .withDefaultValue(targetUser.getProfile().getUserName())
                .withMinLength(1)
                .read("ユーザ名");

        // 電話番号の入力
        String phoneNumber = TextIoUtils.newStringInputReader()
                .withDefaultValue(targetUser.getProfile().getPhoneNumber())
                .withMaxLength(14)
                .withPattern(PatternMessage.PHONE_NUMBER)
                .read("電話番号（省略可）");

        // 連絡先の入力
        String contact = TextIoUtils.newStringInputReader()
                .withDefaultValue(targetUser.getProfile().getContact())
                .withMaxLength(15)
                .read("連絡先（省略可）");

        // 会員種別の入力
        UserType userType = TextIoUtils.newEnumInputReader(UserType.class)
                .withDefaultValue(targetUser.getUserType())
                .read("権限");

        UserUpdateCommand command = UserUpdateCommand.builder()
                .password(password)
                .userName(userName)
                .phoneNumber(phoneNumber)
                .contact(contact)
                .userType(userType)
                .build();

        // ユーザ情報の更新実行
        try {
            UserReference updatedUser = service.update(command);
            this.printResultInformation(updatedUser);
            return Transition.ADMIN_MAIN;

        } catch (BusinessFlowException e) {
            TextIoUtils.printServerError(e);
            return play(loginUser, false); // start over!!

        }
    }

    private void printResultInformation(UserReference updatedUserAccount) {
        TextIoUtils.blankLine();
        TextIoUtils.println("***** ユーザ登録結果 *****");
        TextIoUtils.printf("[%s]のユーザ情報を更新しました", updatedUserAccount.getId());
        TextIoUtils.blankLine();
        TextIoUtils.blankLine();
        TextIoUtils.waitPressEnter();
    }
}
