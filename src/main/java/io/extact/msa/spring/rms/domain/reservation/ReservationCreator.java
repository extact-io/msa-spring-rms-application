package io.extact.msa.spring.rms.domain.reservation;

import jakarta.validation.groups.Default;

import org.springframework.stereotype.Service;

import io.extact.msa.spring.platform.fw.domain.constraint.ValidationGroups.Add;
import io.extact.msa.spring.platform.fw.domain.model.ModelValidator;
import io.extact.msa.spring.platform.fw.domain.service.IdentityGenerator;
import io.extact.msa.spring.rms.domain.item.model.ItemId;
import io.extact.msa.spring.rms.domain.reservation.model.Reservation;
import io.extact.msa.spring.rms.domain.reservation.model.Reservation.ReservationCreatable;
import io.extact.msa.spring.rms.domain.reservation.model.ReservationId;
import io.extact.msa.spring.rms.domain.reservation.model.ReservationPeriod;
import io.extact.msa.spring.rms.domain.user.model.UserId;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReservationCreator {

    private final IdentityGenerator idGenerator;
    private final ModelValidator Validator;
    private final ReservationCreatable constructorProxy = new ReservationCreatable() {};

    public Reservation create(ReservationModelAttributes attrs) {

        ReservationId id = new ReservationId(idGenerator.nextIdentity());
        Reservation reservation = constructorProxy.newInstance(
                id,
                attrs.period,
                attrs.note,
                attrs.itemId,
                attrs.reserverId);

        reservation.configureValidator(Validator);
        Validator.validateModel(reservation, Default.class, Add.class);

        return reservation;
    }

    @Builder
    public static class ReservationModelAttributes {

        private ReservationPeriod period;
        private String note;

        private ItemId itemId;
        private UserId reserverId;
    }
}
