package io.extact.msa.spring.rms.domain.reservation;

import java.util.List;
import java.util.function.Predicate;

import io.extact.msa.spring.platform.fw.domain.service.DuplicateChecker;
import io.extact.msa.spring.platform.fw.exception.BusinessFlowException;
import io.extact.msa.spring.platform.fw.exception.BusinessFlowException.CauseType;
import io.extact.msa.spring.rms.domain.reservation.model.Reservation;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ReservationDuplicateChecker implements DuplicateChecker<Reservation> {

    private final ReservationRepository repository;

    @Override
    public void check(Reservation checkReservation) {

        List<Reservation> overlapped = repository
                .findOverlappingReservations(
                        checkReservation.getItemId(),
                        checkReservation.getReservePeriod());

        overlapped.stream()
                .filter(Predicate.not(checkReservation::equals)) // 自分自身以外があったら
                .findAny()
                .ifPresent(match -> {
                    throw new BusinessFlowException("Already reserved.", CauseType.DUPLICATE);
                });
    }
}
