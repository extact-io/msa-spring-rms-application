package io.extact.msa.spring.rms.domain.reservation.model;

import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

import org.apache.commons.lang3.Range;

import io.extact.msa.spring.platform.fw.domain.constraint.ValidationGroups.Add;
import io.extact.msa.spring.platform.fw.domain.model.ValueModel;
import io.extact.msa.spring.rms.domain.reservation.constraint.BeforeAfterDateTime;
import io.extact.msa.spring.rms.domain.reservation.constraint.BeforeAfterDateTime.BeforeAfterDateTimeValidatable;
import io.extact.msa.spring.rms.domain.reservation.constraint.FromDateTime;
import io.extact.msa.spring.rms.domain.reservation.constraint.FromDateTimeFuture;
import io.extact.msa.spring.rms.domain.reservation.constraint.ToDateTime;
import lombok.Getter;
import lombok.Value;

@Value
@BeforeAfterDateTime
public class ReservationPeriod implements ValueModel, BeforeAfterDateTimeValidatable {

    @FromDateTime
    @FromDateTimeFuture(groups = Add.class)
    @Getter
    private LocalDateTime from;
    @ToDateTime
    @Getter
    private LocalDateTime to;

    public ReservationPeriod(LocalDateTime fromDateTime, LocalDateTime toDateTime) {

        Objects.requireNonNull(fromDateTime, "fromDateTime");
        Objects.requireNonNull(toDateTime, "toDateTime");

        this.from = fromDateTime.truncatedTo(ChronoUnit.MINUTES);
        this.to = toDateTime.truncatedTo(ChronoUnit.MINUTES);
    }

    public boolean isOverlappedBy(ReservationPeriod otherPeriod) {

        Range<ChronoLocalDateTime<?>> range = Range.between(from, to);
        Range<ChronoLocalDateTime<?>> otherRange = Range.between(otherPeriod.from, otherPeriod.to);

        return range.isOverlappedBy(otherRange);
    }
}
