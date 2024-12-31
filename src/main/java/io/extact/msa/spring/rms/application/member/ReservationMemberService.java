package io.extact.msa.spring.rms.application.member;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import io.extact.msa.spring.platform.core.auth.RmsAuthentication;
import io.extact.msa.spring.platform.fw.domain.type.DateTimePeriod;
import io.extact.msa.spring.platform.fw.exception.BusinessFlowException;
import io.extact.msa.spring.platform.fw.exception.BusinessFlowException.CauseType;
import io.extact.msa.spring.rms.application.support.ReservationComposeModel;
import io.extact.msa.spring.rms.application.support.ReservationModelComposer;
import io.extact.msa.spring.rms.domain.item.ItemRepository;
import io.extact.msa.spring.rms.domain.item.model.Item;
import io.extact.msa.spring.rms.domain.item.model.ItemId;
import io.extact.msa.spring.rms.domain.reservation.ReservationCreator;
import io.extact.msa.spring.rms.domain.reservation.ReservationCreator.ReservationModelAttributes;
import io.extact.msa.spring.rms.domain.reservation.ReservationDuplicateChecker;
import io.extact.msa.spring.rms.domain.reservation.ReservationRepository;
import io.extact.msa.spring.rms.domain.reservation.model.Reservation;
import io.extact.msa.spring.rms.domain.reservation.model.ReservationId;
import io.extact.msa.spring.rms.domain.user.UserRepository;
import io.extact.msa.spring.rms.domain.user.model.User;
import io.extact.msa.spring.rms.domain.user.model.UserId;
import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class ReservationMemberService {

    private final ReservationCreator modelCreator;
    private final ReservationModelComposer modelComposer;
    private final ReservationDuplicateChecker duplicateChecker;
    private final ReservationRepository reservationRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;


    public List<Item> getItemAll() {
        return itemRepository.findAll();
    }

    public List<Item> findCanRentedItemAtPeriod(LocalDateTime from, LocalDateTime to) {

        DateTimePeriod overlapPeriod = new DateTimePeriod(from, to);
        List<ItemId> reservedItemIds = reservationRepository
                .findOverlappingReservations(overlapPeriod)
                .stream()
                .map(Reservation::getItemId)
                .toList();

        return itemRepository
                .findAll()
                .stream()
                .filter(item -> !reservedItemIds.contains(item.getId()))
                .toList();
    }

    public boolean canRentedItemAtPeriod(ItemId itemId, LocalDateTime from, LocalDateTime to) {
        DateTimePeriod overlapPeriod = new DateTimePeriod(from, to);
        return reservationRepository
                .findOverlappingReservations(itemId, overlapPeriod)
                .isEmpty();
    }

    public List<ReservationComposeModel> findReservationByItemId(ItemId itemId) {
        return reservationRepository
                .findByRentalItemId(itemId)
                .stream()
                .map(modelComposer::composeModel)
                .toList();
    }

    public List<ReservationComposeModel> findReservationByItemIdAndFromDate(ItemId itemId, LocalDate from) {
        return reservationRepository
                .findByItemIdAndFromDate(itemId, from)
                .stream()
                .map(modelComposer::composeModel)
                .toList();
    }

    public List<ReservationComposeModel> findReservationByReserverId(UserId reserverId) {
        return reservationRepository
                .findByReserverId(reserverId)
                .stream()
                .map(modelComposer::composeModel)
                .toList();
    }

    public List<ReservationComposeModel> getOwnReservations() {
        int userId = getLoginUserId();
        return this.findReservationByReserverId(new UserId(userId));
    }

    public ReservationComposeModel reserve(ReserveItemCommand command) {

        ReservationModelAttributes attrs = ReservationModelAttributes.builder()
                .fromDateTime(command.fromDateTime())
                .toDateTime(command.toDateTime())
                .note(command.note())
                .build();

        Reservation newReservation = modelCreator.create(attrs);

        duplicateChecker.check(newReservation);
        valiateRelation(newReservation.getItemId(), newReservation.getReserverId());

        return modelComposer.composeModel(newReservation);
    }

    public void cancel(ReservationId id) {

        Optional<Reservation> optCancelTarget = reservationRepository.find(id);
        if (optCancelTarget.isEmpty()) {
            throw new BusinessFlowException("Reservation does not exist for reservationId", CauseType.NOT_FOUND);
        }

        Reservation cancelTarget = optCancelTarget.get();
        int cancelingUser = getLoginUserId();

        if (cancelTarget.getReserverId().id() != cancelingUser) {
            throw new BusinessFlowException(
                    String.format("Others' reservations cannot be deleted. reserverId=%s, cancelUserId=%s",
                            cancelTarget.getReserverId().id(),
                            cancelingUser),
                    CauseType.FORBIDDEN);
        }

        reservationRepository.delete(cancelTarget);
    }

    private void valiateRelation(ItemId itemId, UserId reserverId) {

        Optional<Item> shouldExistItem = itemRepository.find(itemId);
        if (shouldExistItem.isEmpty()) {
            throw new BusinessFlowException("RentalItem does not exist for rentalItemId.", CauseType.NOT_FOUND);
        }

        Optional<User> shouldExistUser = userRepository.find(reserverId);
        if (shouldExistUser.isEmpty()) {
            throw new BusinessFlowException("UserAccount does not exist for userAccountId.", CauseType.NOT_FOUND);
        }
    }

    private int getLoginUserId() {
        // TODO アプリ向けの被せものを作る
        RmsAuthentication auth = (RmsAuthentication)SecurityContextHolder.getContext().getAuthentication();
        int userId = auth.getLoginUser().getUserId();
        return userId;
    }
}
