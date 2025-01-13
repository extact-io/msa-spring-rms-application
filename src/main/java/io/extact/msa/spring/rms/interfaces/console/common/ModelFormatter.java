package io.extact.msa.spring.rms.interfaces.console.common;

import io.extact.msa.spring.rms.application.support.ReservationComposeModel;
import io.extact.msa.spring.rms.domain.item.model.ItemReference;
import io.extact.msa.spring.rms.domain.user.model.UserReference;

public interface ModelFormatter<T> {

    String format(T dto);

    static class RentalItemFormatter implements ModelFormatter<ItemReference> {
        @Override
        public String format(ItemReference item) {
            return String.format("[%s]%s シリアル番号：%s",
                    item.getId().id(),
                    item.getItemName(),
                    item.getSerialNo());
        }
    }

    static class ReservationFormatter implements ModelFormatter<ReservationComposeModel> {
        @Override
        public String format(ReservationComposeModel model) {
            return String.format("[%s] %s - %s %s %s %s",
                    model.reservation().getId().id(),
                    ClientConstants.DATETIME_FORMAT.format(model.reservation().getPeriod().getFrom()),
                    ClientConstants.DATETIME_FORMAT.format(model.reservation().getPeriod().getTo()),
                    model.rentalItem().getItemName(),
                    model.reserver().getProfile().getUserName(),
                    model.reservation().getNote());
        }
    }

    static class UserAccountFormatter implements ModelFormatter<UserReference> {
        @Override
        public String format(UserReference user) {
            return String.format("[%s] %s/%s %s %s",
                    user.getId(),
                    user.getLoginId(),
                    user.getPassword(),
                    user.getProfile().getUserName(),
                    user.getUserType());
        }
    }
}
