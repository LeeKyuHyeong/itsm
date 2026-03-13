package com.itsm.api.dto.incident;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyTrendItem {

    private String month;  // "2026-01", "2026-02", etc.
    private long incidentCount;
    private long srCount;
    private long changeCount;
}
