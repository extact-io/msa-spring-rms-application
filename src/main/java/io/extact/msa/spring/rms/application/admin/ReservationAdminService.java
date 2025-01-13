package io.extact.msa.spring.rms.application.admin;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import io.extact.msa.spring.platform.fw.application.ApplicationCrudSupport;
import io.extact.msa.spring.rms.application.support.ReservationComposeModel;
import io.extact.msa.spring.rms.application.support.ReservationModelComposer;
import io.extact.msa.spring.rms.domain.reservation.ReservationDuplicateChecker;
import io.extact.msa.spring.rms.domain.reservation.ReservationRepository;
import io.extact.msa.spring.rms.domain.reservation.model.Reservation;
import io.extact.msa.spring.rms.domain.reservation.model.ReservationId;
import io.extact.msa.spring.rms.domain.reservation.model.ReservationReference;

@Transactional
public class ReservationAdminService {

    private final ReservationModelComposer modelComposer;
    private final ApplicationCrudSupport<Reservation> support;

    public ReservationAdminService(
            ReservationDuplicateChecker duplicateChecker,
            ReservationModelComposer modelComposer,
            ReservationRepository repository) {

        this.modelComposer = modelComposer;
        this.support = new ApplicationCrudSupport<>(duplicateChecker, repository);
    }

    public List<ReservationComposeModel> getAll() {
        return support
                .getAll()
                .stream()
                .map(modelComposer::composeModel)
                .toList();
    }

    public ReservationComposeModel update(ReservationUpdateCommand command) {
        ReservationReference updated = support.update(command.id(), reservation -> this.editModel(reservation, command));
        return modelComposer.composeModel(updated);
    }

    public void delete(ReservationId id) {
        support.delete(id);
    }

    private void editModel(Reservation reservation, ReservationUpdateCommand command) {
        reservation.editReservation(
                command.period(),
                command.note());
    }
}
