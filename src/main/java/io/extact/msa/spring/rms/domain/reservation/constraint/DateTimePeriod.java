package io.extact.msa.spring.rms.domain.reservation.constraint;

import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDateTime;

import org.apache.commons.lang3.Range;

public class DateTimePeriod {

    private LocalDateTime fromDateTime;
    private LocalDateTime toDateTime;
    private Range<ChronoLocalDateTime<?>> period;

    public DateTimePeriod(LocalDateTime fromDateTime, LocalDateTime toDateTime) {
        this.fromDateTime = fromDateTime;
        this.toDateTime = toDateTime;
        period = Range.between(fromDateTime, toDateTime);
    }

    public LocalDateTime getFromDateTime() {
        return this.fromDateTime;
    }

    public LocalDateTime getToDateTime() {
        return this.toDateTime;
    }

    public boolean isOverlappedBy(DateTimePeriod otherPeriod) {
        return this.period.isOverlappedBy(otherPeriod.period);
    }
}
