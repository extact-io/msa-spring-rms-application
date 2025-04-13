package io.extact.msa.spring.rms.domain.reservation.model;

import java.util.Objects;

import io.extact.msa.spring.platform.fw.domain.model.EntityModelView;
import io.extact.msa.spring.rms.domain.item.model.ItemId;
import io.extact.msa.spring.rms.domain.user.model.UserId;

/**
 * ReservationModelのViewインタフェース。
 * EntityModelの同値性はIDのみで確認する一方、EntityModelViewの場合はすべての値が等しいかで
 * 比較したいためIsEqualableを実装している。
 */
public interface ReservationModelView extends EntityModelView<ReservationModelView> {

    ReservationId getId();

    ReservationPeriod getPeriod();

    String getNote();

    UserId getReserverId();

    ItemId getItemId();

    @Override
    default boolean isEqual(ReservationModelView other) {
        if (other == null) {
            return false;
        }
        return Objects.equals(getId(), other.getId())
                && Objects.equals(getPeriod(), other.getPeriod())
                && Objects.equals(getNote(), other.getNote())
                && Objects.equals(getItemId(), other.getItemId())
                && Objects.equals(getReserverId(), other.getReserverId());
    }
}