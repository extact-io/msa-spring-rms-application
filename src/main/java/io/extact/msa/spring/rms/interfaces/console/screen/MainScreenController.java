package io.extact.msa.spring.rms.interfaces.console.screen;

import io.extact.msa.spring.platform.core.env.MainModuleInformation;
import io.extact.msa.spring.rms.application.admin.ItemAdminService;
import io.extact.msa.spring.rms.application.admin.ReservationAdminService;
import io.extact.msa.spring.rms.application.admin.UserAdminService;
import io.extact.msa.spring.rms.application.member.ReservationMemberService;
import io.extact.msa.spring.rms.application.universal.LoginService;
import io.extact.msa.spring.rms.domain.user.model.UserReference;
import io.extact.msa.spring.rms.interfaces.console.screen.TransitionMap.Transition;
import io.extact.msa.spring.rms.interfaces.console.screen.admin.AddItemScreen;
import io.extact.msa.spring.rms.interfaces.console.screen.admin.AddUserScreen;
import io.extact.msa.spring.rms.interfaces.console.screen.admin.AdminMainScreen;
import io.extact.msa.spring.rms.interfaces.console.screen.admin.UpdateUserScreen;
import io.extact.msa.spring.rms.interfaces.console.screen.login.EndScreen;
import io.extact.msa.spring.rms.interfaces.console.screen.login.LoginEventObserver;
import io.extact.msa.spring.rms.interfaces.console.screen.login.LoginScreen;
import io.extact.msa.spring.rms.interfaces.console.screen.member.CancelReservationScreen;
import io.extact.msa.spring.rms.interfaces.console.screen.member.InquiryReservationScreen;
import io.extact.msa.spring.rms.interfaces.console.screen.member.MemberMainScreen;
import io.extact.msa.spring.rms.interfaces.console.screen.member.ReserveItemScreen;

/**
 * アプリケーションの画面遷移制御クラス。
 */
public class MainScreenController implements LoginEventObserver {

    private final TransitionMap transitionMap;
    private UserReference currentLoginUser;

    public MainScreenController(
            LoginService loginService,
            ItemAdminService itemAdminService,
            ReservationAdminService reservationAdminService,
            UserAdminService userAdminService,
            ReservationMemberService reservationMemberService,
            MainModuleInformation moduleInfo) {

        this.transitionMap = new TransitionMap();

        transitionMap.add(
                Transition.LOGIN,
                new LoginScreen(
                        loginService,
                        this,
                        moduleInfo));
        transitionMap.add(
                Transition.MEMBER_MAIN,
                new MemberMainScreen());
        transitionMap.add(
                Transition.INQUIRY_RESERVATION,
                new InquiryReservationScreen(reservationMemberService));
        transitionMap.add(
                Transition.ENTRY_RESERVATRION,
                new ReserveItemScreen(reservationMemberService));
        transitionMap.add(
                Transition.CANCEL_RESERVATRION,
                new CancelReservationScreen(reservationMemberService));
        transitionMap.add(
                Transition.ADMIN_MAIN,
                new AdminMainScreen());
        transitionMap.add(
                Transition.ENTRY_RENTAL_ITEM,
                new AddItemScreen(itemAdminService));
        transitionMap.add(
                Transition.ENTRY_USER,
                new AddUserScreen(userAdminService));
        transitionMap.add(
                Transition.EDIT_USER,
                new UpdateUserScreen(userAdminService));
        transitionMap.add(
                Transition.END,
                new EndScreen());
    }

    public void start() {
        var loginScreen = transitionMap.stratScreen();
        doPlay(loginScreen);
    }

    private RmsScreen doPlay(RmsScreen screen) {
        var next = screen.play(currentLoginUser, true);
        return next != null ? doPlay(transitionMap.nextScreen(next)) : null;
    }

    // -------------------------------------------------- obsever methods.

    @Override
    public void onEvent(UserReference loginUser) {
        this.currentLoginUser = loginUser;
    }

    @Override
    public UserReference getLoginUser() {
        return this.currentLoginUser;
    }
}
