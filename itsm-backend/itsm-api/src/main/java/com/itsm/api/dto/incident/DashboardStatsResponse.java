package com.itsm.api.dto.incident;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {

    private Map<String, Long> statusCounts;
    private Map<String, Long> priorityCounts;
    private long slaOverdueCount;
    private long slaWarningCount;
    private List<IncidentResponse> recentIncidents;
}
