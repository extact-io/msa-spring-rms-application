package io.extact.msa.spring.rms.application.support;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import io.extact.msa.spring.platform.core.async.AsyncInvoker;
import io.extact.msa.spring.platform.fw.exception.BusinessFlowException;
import io.extact.msa.spring.platform.fw.exception.BusinessFlowException.CauseType;
import io.extact.msa.spring.rms.domain.item.ItemRepository;
import io.extact.msa.spring.rms.domain.item.model.Item;
import io.extact.msa.spring.rms.domain.item.model.ItemModelView;
import io.extact.msa.spring.rms.domain.reservation.model.ReservationModelView;
import io.extact.msa.spring.rms.domain.user.UserRepository;
import io.extact.msa.spring.rms.domain.user.model.User;
import io.extact.msa.spring.rms.domain.user.model.UserModelView;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ReservationModelComposer {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    private final AsyncInvoker asyncInvoker;

    public ReservationComposeModel composeModel(ReservationModelView reservation) {

        CompletableFuture<Optional<Item>> itemFuture = asyncInvoker
                .invoke(() -> itemRepository.find(reservation.getItemId()));
        CompletableFuture<Optional<User>> userFuture = asyncInvoker
                .invoke(() -> userRepository.find(reservation.getReserverId()));

        CompletableFuture.allOf(itemFuture, userFuture).join();

        ItemModelView item = itemFuture.join()
                .orElseThrow(() -> new BusinessFlowException(
                        "target item does not exist for id:[" + reservation.getItemId() + "]", CauseType.NOT_FOUND));
        UserModelView user = userFuture.join()
                .orElseThrow(() -> new BusinessFlowException(
                        "target user does not exist for id:[" + reservation.getReserverId() + "]", CauseType.NOT_FOUND));

        return new ReservationComposeModel(reservation, item, user);
    }
}
