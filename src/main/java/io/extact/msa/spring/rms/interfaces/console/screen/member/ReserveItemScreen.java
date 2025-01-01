package io.extact.msa.spring.rms.interfaces.console.screen.member;

import static io.extact.msa.spring.rms.interfaces.console.common.ClientConstants.*;

import java.time.LocalDateTime;
import java.util.List;

import io.extact.msa.spring.platform.fw.exception.BusinessFlowException;
import io.extact.msa.spring.rms.application.member.ReservationMemberService;
import io.extact.msa.spring.rms.application.member.ReserveItemCommand;
import io.extact.msa.spring.rms.application.support.ReservationComposeModel;
import io.extact.msa.spring.rms.domain.item.model.ItemReference;
import io.extact.msa.spring.rms.domain.user.model.UserReference;
import io.extact.msa.spring.rms.interfaces.console.screen.RmsScreen;
import io.extact.msa.spring.rms.interfaces.console.screen.TransitionMap.Transition;
import io.extact.msa.spring.rms.interfaces.console.textio.TextIoUtils;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ReserveItemScreen implements RmsScreen {

    private final ReservationMemberService service;

    @Override
    public Transition play(UserReference loginUser, boolean printHeader) {

        if (printHeader) {
            TextIoUtils.printScreenHeader(loginUser, "レンタル品予約画面");
        }

        // レンタル品一覧を表示
        List<? extends ItemReference> items = service.getItemAll();
        TextIoUtils.println(ENTRY_RESERVATION_INFORMATION);
        items.forEach(dto ->
        TextIoUtils.println(ITEM_FORMAT.format(dto))
        );
        TextIoUtils.blankLine();

        // 予約するレンタル品の選択
        int selectedItem = TextIoUtils.newIntInputReader()
                .withSelectableValues(
                        items.stream()
                            .map(item -> item.getId().id())
                            .toList(),
                        SCREEN_BREAK_VALUE)
                .read("レンタル品番号");
        if (TextIoUtils.isBreak(selectedItem)) {
            return Transition.MEMBER_MAIN;
        }

        // 利用開始日時の入力
        LocalDateTime fromDateTime = TextIoUtils.newLocalDateTimeReader()
                .withFutureNow()
                .read("利用開始日時（入力例－2020/04/01 09:00）:");

        // 利用終了日時の入力
        LocalDateTime toDateTime = TextIoUtils.newLocalDateTimeReader()
                .withFutureThan(fromDateTime)
                .read("利用終了日時（入力例－2020/04/01 18:00）:");

        // 備考の入力
        String note = TextIoUtils.newStringInputReader()
                .withMaxLength(15)
                .withDefaultValue("")
                .read("備考（空白可）");

        TextIoUtils.blankLine();

        ReserveItemCommand command = ReserveItemCommand.builder()
                .fromDateTime(fromDateTime)
                .toDateTime(toDateTime)
                .note(note)
                .build();

        // レンタル品予約の実行
        try {
            ReservationComposeModel newReservation = service.reserve(command);
            this.printResultInformation(newReservation);
            return Transition.MEMBER_MAIN;

        } catch (BusinessFlowException e) {
            TextIoUtils.printServerError(e);
            return play(loginUser, false); // start over!!

        }
    }

    private void printResultInformation(ReservationComposeModel newReservation) {
        TextIoUtils.println("***** 予約確定結果 *****");
        TextIoUtils.printf(RESERVATION_FORMAT.format(newReservation));
        TextIoUtils.blankLine();
        TextIoUtils.blankLine();
        TextIoUtils.waitPressEnter();
    }
}
