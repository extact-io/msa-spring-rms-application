package io.extact.msa.spring.rms.interfaces.console.screen.member;

import static io.extact.msa.spring.rms.interfaces.console.common.ClientConstants.*;

import java.time.LocalDate;
import java.util.List;

import io.extact.msa.spring.rms.application.member.ReservationMemberService;
import io.extact.msa.spring.rms.application.support.ReservationComposeModel;
import io.extact.msa.spring.rms.domain.item.model.ItemId;
import io.extact.msa.spring.rms.domain.item.model.ItemReference;
import io.extact.msa.spring.rms.domain.user.model.UserReference;
import io.extact.msa.spring.rms.interfaces.console.screen.RmsScreen;
import io.extact.msa.spring.rms.interfaces.console.screen.TransitionMap.Transition;
import io.extact.msa.spring.rms.interfaces.console.textio.TextIoUtils;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class InquiryReservationScreen implements RmsScreen {

    private final ReservationMemberService service;

    @Override
    public Transition play(UserReference loginUser, boolean printHeader) {

        if (printHeader) {
            TextIoUtils.printScreenHeader(loginUser, "予約照会画面");
        }

        // レンタル品一覧を表示
        List<? extends ItemReference> items = service.getItemAll();
        TextIoUtils.println(INQUIRY_RESERVATION_INFORMATION);
        items.forEach(dto -> TextIoUtils.println(ITEM_FORMAT.format(dto)));
        TextIoUtils.blankLine();

        // 照会するレンタル品を選択
        int selectedItemId = TextIoUtils.newIntInputReader()
                .withSelectableValues(
                        items.stream()
                                .map(item -> item.getId().id())
                                .toList(),
                        SCREEN_BREAK_VALUE)
                .read("レンタル品番号");
        if (TextIoUtils.isBreak(selectedItemId)) {
            return Transition.MEMBER_MAIN;
        }

        // 照会する日付を入力
        var inputedDate = TextIoUtils.newLocalDateReader()
                .read("日付（入力例－2020/10/23）");

        // 照会の実行
        List<ReservationComposeModel> results = service
                .findReservationByItemIdAndFromDate(
                        new ItemId(selectedItemId),
                        inputedDate);

        // 該当データなしの場合は最初から
        if (results.isEmpty()) {
            TextIoUtils.printErrorInformation(DATA_NOT_FOUND_INFORMATION);
            return play(loginUser, false); // start over!!
        }
        // 該当データありの場合は結果出力してメインメニューへ
        this.printResultList(selectedItemId, inputedDate, results);
        return Transition.MEMBER_MAIN;
    }

    private void printResultList(int selectedItem, LocalDate inputedDate, List<ReservationComposeModel> reservations) {
        TextIoUtils.blankLine();
        TextIoUtils.println("***** 予約検索結果 *****");
        TextIoUtils.println("選択レンタル品番号：" + selectedItem);
        TextIoUtils.println("入力日付：" + DATE_FORMAT.format(inputedDate));
        reservations.forEach(r -> TextIoUtils.println(RESERVATION_FORMAT.format(r)));
        TextIoUtils.blankLine();
        TextIoUtils.waitPressEnter();
    }
}
