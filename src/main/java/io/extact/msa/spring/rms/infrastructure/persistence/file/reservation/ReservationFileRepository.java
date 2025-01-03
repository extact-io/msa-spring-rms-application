package io.extact.msa.spring.rms.infrastructure.persistence.file.reservation;

import java.time.LocalDate;
import java.util.List;

import io.extact.msa.spring.platform.fw.infrastructure.persistence.file.AbstractFileRepository;
import io.extact.msa.spring.platform.fw.infrastructure.persistence.file.ModelArrayMapper;
import io.extact.msa.spring.platform.fw.infrastructure.persistence.file.io.FileOperator;
import io.extact.msa.spring.rms.domain.item.model.ItemId;
import io.extact.msa.spring.rms.domain.reservation.ReservationRepository;
import io.extact.msa.spring.rms.domain.reservation.model.Reservation;
import io.extact.msa.spring.rms.domain.user.model.UserId;

public class ReservationFileRepository extends AbstractFileRepository<Reservation> implements ReservationRepository {

    public static final String FILE_ENTITY = "reservation";

    public ReservationFileRepository(FileOperator fileReadWriter, ModelArrayMapper<Reservation> mapper) {
        super(fileReadWriter, mapper);
    }

    @Override
    public String getEntityName() {
        return FILE_ENTITY;
    }

    @Override
    public List<Reservation> findByItemIdAndFromDate(ItemId itemId, LocalDate from) {
        return this.findAll().stream()
                .filter(reservation -> reservation.getFromDateTime().toLocalDate().equals(from))
                .filter(reservation -> reservation.getItemId().equals(itemId))
                .toList();
    }

    @Override
    public List<Reservation> findByReserverId(UserId reserverId) {
        return this.findAll().stream()
                .filter(reservation -> reservation.getReserverId().equals(reserverId))
                .toList();
    }

    @Override
    public List<Reservation> findByItemId(ItemId itemId) {
        return this.findAll().stream()
                .filter(reservation -> reservation.getItemId().equals(itemId))
                .toList();
    }
}
