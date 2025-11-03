package io.extact.msa.spring.rms.webapi.member;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

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

class ReserveItemResponseTest {

    private static final ItemCreatable itemCreator = new ItemCreatable() {};
    private static final UserCreatable userCreator = new UserCreatable() {};
    private static final ReservationCreatable reservationCreator = new ReservationCreatable() {};

    @Test
    void testFrom() {
        // given
        Reservation reservation = reservationCreator.newInstance(
                new ReservationId(1),
                new ReservationPeriod(
                        LocalDateTime.of(2099, 1, 1, 10, 0),
                        LocalDateTime.of(2099, 1, 1, 12, 0)),
                "Test Note",
                new ItemId(101),
                new UserId(201));

        Item item = itemCreator.newInstance(new ItemId(101), "Item 101", "name");
        User reserver = userCreator.newInstance(new UserId(201), "user201", "password", UserType.MEMBER,
                "Reserver Name", "123456789", "reserver@example.com");

        ReservationComposeModel model = new ReservationComposeModel(reservation, item, reserver);

        // when
        ReserveItemResponse response = ReserveItemResponse.from(model);

        // then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1);
        assertThat(response.fromDateTime()).isEqualTo(LocalDateTime.of(2099, 1, 1, 10, 0));
        assertThat(response.toDateTime()).isEqualTo(LocalDateTime.of(2099, 1, 1, 12, 0));
        assertThat(response.note()).isEqualTo("Test Note");
        assertThat(response.itemId()).isEqualTo(101);
        assertThat(response.serialNo()).isEqualTo("Item 101");
        assertThat(response.itemName()).isEqualTo("name");
        assertThat(response.reserverId()).isEqualTo(201);
    }

    @Test
    void testFromNull() {
        // given
        ReservationComposeModel model = null;
        // when
        ReserveItemResponse response = ReserveItemResponse.from(model);
        // then
        assertThat(response).isNull();
    }
}
