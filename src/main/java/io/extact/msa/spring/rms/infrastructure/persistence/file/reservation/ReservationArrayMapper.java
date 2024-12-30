package io.extact.msa.spring.rms.infrastructure.persistence.file.reservation;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import io.extact.msa.spring.platform.fw.infrastructure.persistence.file.ModelArrayMapper;
import io.extact.msa.spring.rms.domain.item.model.ItemId;
import io.extact.msa.spring.rms.domain.reservation.model.Reservation;
import io.extact.msa.spring.rms.domain.reservation.model.Reservation.ReservationCreatable;
import io.extact.msa.spring.rms.domain.reservation.model.ReservationId;
import io.extact.msa.spring.rms.domain.user.model.UserId;

public class ReservationArrayMapper implements ModelArrayMapper<Reservation>, ReservationCreatable {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
    public static final ReservationArrayMapper INSTANCE = new ReservationArrayMapper();

    @Override
    public Reservation toModel(String[] attributes) {

        int id = Integer.parseInt(attributes[0]);
        LocalDateTime from = LocalDateTime.parse(attributes[1], DATE_TIME_FORMATTER);
        LocalDateTime to = LocalDateTime.parse(attributes[2], DATE_TIME_FORMATTER);
        String note = attributes[3];
        int itemId = Integer.parseInt(attributes[4]);
        int reserverId = Integer.parseInt(attributes[5]);

        return newInstance(
                new ReservationId(id),
                from,
                to,
                note,
                new ItemId(itemId),
                new UserId(reserverId));
    }

    @Override
    public String[] toArray(Reservation reservation) {

        String[] attributes = new String[7];

        attributes[0] = String.valueOf(reservation.getId());
        attributes[1] = DATE_TIME_FORMATTER.format(reservation.getFromDateTime());
        attributes[2] = DATE_TIME_FORMATTER.format(reservation.getToDateTime());
        attributes[3] = reservation.getNote();
        attributes[4] = String.valueOf(reservation.getItemId());
        attributes[5] = String.valueOf(reservation.getReserverId());

        return attributes;
    }
}
