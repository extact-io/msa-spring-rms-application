package io.extact.msa.spring.rms.interfaces.console.screen.admin;

import static io.extact.msa.spring.rms.interfaces.console.common.ClientConstants.*;

import io.extact.msa.spring.platform.fw.exception.BusinessFlowException;
import io.extact.msa.spring.rms.application.admin.ItemAddCommand;
import io.extact.msa.spring.rms.application.admin.ItemAdminService;
import io.extact.msa.spring.rms.domain.item.model.ItemReference;
import io.extact.msa.spring.rms.domain.user.model.UserReference;
import io.extact.msa.spring.rms.interfaces.console.screen.RmsScreen;
import io.extact.msa.spring.rms.interfaces.console.screen.TransitionMap.Transition;
import io.extact.msa.spring.rms.interfaces.console.textio.RmsStringInputReader.PatternMessage;
import io.extact.msa.spring.rms.interfaces.console.textio.TextIoUtils;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AddItemScreen implements RmsScreen {

    private final ItemAdminService service;

    @Override
    public Transition play(UserReference loginUser, boolean printHeader) {

        if (printHeader) {
            TextIoUtils.printScreenHeader(loginUser, "レンタル品登録画面");
        }

        // 入力インフォメーションの表示
        TextIoUtils.println(ENTRY_RENTAL_ITEM_INFORMATION);

        // シリアル番号の入力
        String serialNo = TextIoUtils.newStringInputReader()
                .withMinLength(1)
                .withMaxLength(15)
                .withPattern(PatternMessage.SERIAL_NO)
                .read("シリアル番号");
        if (TextIoUtils.isBreak(serialNo)) {
            return Transition.ADMIN_MAIN;
        }

        // 品名の入力
        String itemName = TextIoUtils.newStringInputReader()
                .withMaxLength(15)
                .withDefaultValue("")
                .read("品名（空白可）");

        TextIoUtils.blankLine();

        ItemAddCommand command = ItemAddCommand.builder()
                .serialNo(serialNo)
                .itemName(itemName)
                .build();

        // レンタル品登録の実行
        try {
            ItemReference newItem = service.add(command);
            this.printResultInformation(newItem);
            return Transition.ADMIN_MAIN;

        } catch (BusinessFlowException e) {
            TextIoUtils.printServerError(e);
            return play(loginUser, false); // start over!!

        }
    }

    private void printResultInformation(ItemReference newItem) {
        TextIoUtils.println("***** レンタル品登録結果 *****");
        TextIoUtils.printf(ITEM_FORMAT.format(newItem));
        TextIoUtils.blankLine();
        TextIoUtils.waitPressEnter();
    }
}
