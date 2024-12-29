package io.extact.msa.spring.rms.application.admin;

import java.util.List;
import java.util.Optional;

import io.extact.msa.spring.rms.application.support.ApplicationCrudSupport;
import io.extact.msa.spring.rms.application.support.ReservationComposeModel;
import io.extact.msa.spring.rms.application.support.ReservationModelComposer;
import io.extact.msa.spring.rms.domain.reservation.ReservationDuplicateChecker;
import io.extact.msa.spring.rms.domain.reservation.ReservationRepository;
import io.extact.msa.spring.rms.domain.reservation.model.Reservation;
import io.extact.msa.spring.rms.domain.reservation.model.ReservationId;

public class ReservationManagementService {

    private final ReservationRepository repository;
    private final ReservationModelComposer modelComposer;
    private final ApplicationCrudSupport<Reservation> support;

    public ReservationManagementService(
            ReservationDuplicateChecker duplicateChecker,
            ReservationModelComposer modelComposer,
            ReservationRepository repository) {

        this.repository = repository;
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

    public Optional<ReservationComposeModel> getById(ReservationId id) {
        return repository
                .find(id)
                .map(modelComposer::composeModel);
    }

    public ReservationComposeModel update(UpdateReservationCommand command) {
        Reservation updated = support.update(command.id(), reservation -> this.editModel(reservation, command));
        return modelComposer.composeModel(updated);
    }

    public void delete(ReservationId id) {
        support.delete(id);
    }

    private void editModel(Reservation reservation, UpdateReservationCommand command) {
        reservation.editReservation(
                command.fromDateTime(),
                command.toDateTime(),
                command.note());
    }
}
