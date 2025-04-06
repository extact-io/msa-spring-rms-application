package io.extact.msa.spring.rms.application.member;

import java.util.List;

import io.extact.msa.spring.rms.domain.reservation.model.ReservationModelView;

public interface ReserveItemQueryService {
    
    /**
     * 検索条件に一致する予約を取得する。
     * 検索条件の予約開始日(from)は予約開始日時がその日になっている予約となる。
     * 
     * @param cond 検索条件
     * @return 該当予約。該当がない場合は空リスト
     */
    List<ReservationModelView> findByCondition(ReserveItemQueryCondition cond);
}
