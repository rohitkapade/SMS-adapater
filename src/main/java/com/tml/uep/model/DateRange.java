package com.tml.uep.model;

import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DateRange {
    private OffsetDateTime fromDateTime;
    private OffsetDateTime toDateTime;
}
