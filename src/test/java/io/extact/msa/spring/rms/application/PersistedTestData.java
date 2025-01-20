package io.extact.msa.spring.rms.application;

import java.time.LocalDateTime;

import io.extact.msa.spring.rms.application.support.ReservationComposeModel;
import io.extact.msa.spring.rms.domain.item.model.Item;
import io.extact.msa.spring.rms.domain.item.model.Item.ItemCreatable;
import io.extact.msa.spring.rms.domain.item.model.ItemId;
import io.extact.msa.spring.rms.domain.reservation.model.Reservation;
import io.extact.msa.spring.rms.domain.reservation.model.Reservation.ReservationCreatable;
import io.extact.msa.spring.rms.domain.reservation.model.ReservationId;
import io.extact.msa.spring.rms.domain.reservation.model.ReservationPeriod;
import io.extact.msa.spring.rms.domain.user.model.User;
import io.extact.msa.spring.rms.domain.user.model.User.UserCreatable;
import io.extact.msa.spring.rms.domain.user.model.UserId;
import io.extact.msa.spring.rms.domain.user.model.UserType;

public class PersistedTestData {

    // --- item data
    private static final ItemCreatable itemCreator = new ItemCreatable() {};

    public static final Item item1 = itemCreator.newInstance(new ItemId(1), "A0001", "レンタル品1号");
    public static final Item item2 = itemCreator.newInstance(new ItemId(2), "A0002", "レンタル品2号");
    public static final Item item3 = itemCreator.newInstance(new ItemId(3), "A0003", "レンタル品3号");
    public static final Item item4 = itemCreator.newInstance(new ItemId(4), "A0004", "レンタル品4号");


    // --- user data
    private static final UserCreatable userCreator = new UserCreatable() {};

    public static final User user1 = userCreator.newInstance(new UserId(1), "member1", "member1", UserType.MEMBER,
            "メンバー1", "070-1111-2222", "連絡先1");
    public static final User user2 = userCreator.newInstance(new UserId(2), "member2", "member2", UserType.MEMBER,
            "メンバー2", "080-1111-2222", "連絡先2");
    public static final User user3 = userCreator.newInstance(new UserId(3), "admin", "admin", UserType.ADMIN,
            "管理者", "050-1111-2222", "連絡先3");


    // --- reservation data
    private static final ReservationCreatable reservationCreator = new ReservationCreatable() {};

    public static final Reservation reservation1 = reservationCreator.newInstance(
            new ReservationId(1),
            new ReservationPeriod(LocalDateTime.of(2020, 4, 1, 10, 0), LocalDateTime.of(2020, 4, 1, 12, 0)),
            "メモ1",
            new ItemId(3),
            new UserId(1));
    public static final Reservation reservation2 = reservationCreator.newInstance(
            new ReservationId(2),
            new ReservationPeriod(LocalDateTime.of(2020, 4, 1, 16, 0), LocalDateTime.of(2020, 4, 1, 18, 0)),
            "メモ2",
            new ItemId(3),
            new UserId(2));
    public static final Reservation reservation3 = reservationCreator.newInstance(
            new ReservationId(3),
            new ReservationPeriod(LocalDateTime.of(2099, 4, 1, 10, 0), LocalDateTime.of(2099, 4, 1, 12, 0)),
            "メモ3",
            new ItemId(3),
            new UserId(1));


    // --- compose model data
    public static final ReservationComposeModel model1 = new ReservationComposeModel(
            reservation1,
            item3,
            user1);
    public static final ReservationComposeModel model2 = new ReservationComposeModel(
            reservation2,
            item3,
            user2);
    public static final ReservationComposeModel model3 = new ReservationComposeModel(
            reservation3,
            item3,
            user1);

}
