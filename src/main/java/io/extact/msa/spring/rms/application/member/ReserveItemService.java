package io.extact.msa.spring.rms.application.member;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.extact.msa.spring.platform.core.auth.context.LoginContext;
import io.extact.msa.spring.platform.fw.application.ApplicationService;
import io.extact.msa.spring.platform.fw.exception.BusinessFlowException;
import io.extact.msa.spring.platform.fw.exception.BusinessFlowException.CauseType;
import io.extact.msa.spring.rms.application.support.ReservationComposeModel;
import io.extact.msa.spring.rms.application.support.ReservationModelComposer;
import io.extact.msa.spring.rms.domain.item.ItemRepository;
import io.extact.msa.spring.rms.domain.item.model.Item;
import io.extact.msa.spring.rms.domain.item.model.ItemId;
import io.extact.msa.spring.rms.domain.item.model.ItemModelView;
import io.extact.msa.spring.rms.domain.reservation.ReservationCreator;
import io.extact.msa.spring.rms.domain.reservation.ReservationCreator.ReservationModelAttributes;
import io.extact.msa.spring.rms.domain.reservation.ReservationDuplicateChecker;
import io.extact.msa.spring.rms.domain.reservation.ReservationRepository;
import io.extact.msa.spring.rms.domain.reservation.model.Reservation;
import io.extact.msa.spring.rms.domain.reservation.model.ReservationId;
import io.extact.msa.spring.rms.domain.reservation.model.ReservationPeriod;
import io.extact.msa.spring.rms.domain.user.UserRepository;
import io.extact.msa.spring.rms.domain.user.model.User;
import io.extact.msa.spring.rms.domain.user.model.UserId;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@ApplicationService
public class ReserveItemService {

    private final LoginContext loginContext;
    private final ReservationCreator modelCreator;
    private final ReservationModelComposer modelComposer;
    private final ReservationDuplicateChecker duplicateChecker;
    private final ReserveItemQueryService queryService;
    private final ReservationRepository reservationRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    public List<ItemModelView> getItemAll() {
        return new ArrayList<>(itemRepository.findAll()); // 型をModelViewに制限するため変換
    }

    public List<ItemModelView> findRentableItemAtPeriod(LocalDateTime from, LocalDateTime to) {

        ReservationPeriod overlapPeriod = new ReservationPeriod(from, to);
        List<ItemId> reservedItemIds = reservationRepository
                .findOverlappingReservations(overlapPeriod)
                .stream()
                .map(Reservation::getItemId)
                .toList();

        List<Item> items = itemRepository
                .findAll()
                .stream()
                .filter(item -> !reservedItemIds.contains(item.getId()))
                .toList();
        return new ArrayList<>(items); // 型をModelViewに制限するため変換
    }

    public boolean isRentableItemAtPeriod(ItemId itemId, LocalDateTime from, LocalDateTime to) {
        ReservationPeriod overlapPeriod = new ReservationPeriod(from, to);
        return reservationRepository
                .findOverlappingReservations(itemId, overlapPeriod)
                .isEmpty();
    }

    public List<ReservationComposeModel> findReservationByCondition(ReserveItemQueryCondition cond) {
        return queryService
                .findByCondition(cond)
                .stream()
                .map(modelComposer::composeModel)
                .toList();
    }

    public List<ReservationComposeModel> getOwnReservations() {
        int userId = loginContext.getLoginUser().getUserId();
        ReserveItemQueryCondition cond = ReserveItemQueryCondition.builder()
                .reserverId(userId)
                .build();
        return this.findReservationByCondition(cond);
    }

    public ReservationComposeModel reserve(ReserveItemCommand command) {

        ReservationModelAttributes attrs = ReservationModelAttributes.builder()
                .period(command.period())
                .note(command.note())
                .itemId(command.itemId())
                .reserverId(new UserId(loginContext.getLoginUser().getUserId()))
                .build();

        Reservation newReservation = modelCreator.create(attrs);

        duplicateChecker.check(newReservation);
        valiateRelation(newReservation.getItemId(), newReservation.getReserverId());

        reservationRepository.add(newReservation);

        return modelComposer.composeModel(newReservation);
    }

    public void cancel(ReservationId id) {

        Optional<Reservation> optCancelTarget = reservationRepository.find(id);
        if (optCancelTarget.isEmpty()) {
            throw new BusinessFlowException("Reservation does not exist for reservationId", CauseType.NOT_FOUND);
        }

        Reservation cancelTarget = optCancelTarget.get();
        int cancelingUser = loginContext.getLoginUser().getUserId();

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
            throw new BusinessFlowException("Item does not exist for itemId.", CauseType.NOT_FOUND);
        }

        Optional<User> shouldExistUser = userRepository.find(reserverId);
        if (shouldExistUser.isEmpty()) {
            throw new BusinessFlowException("User does not exist for reserverId.", CauseType.NOT_FOUND);
        }
    }
}
