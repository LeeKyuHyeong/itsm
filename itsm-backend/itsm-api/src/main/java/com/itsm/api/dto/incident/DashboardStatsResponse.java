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

    // Incident stats (existing)
    private Map<String, Long> statusCounts;
    private Map<String, Long> priorityCounts;
    private long slaOverdueCount;
    private long slaWarningCount;
    private List<IncidentResponse> recentIncidents;

    // Service Request stats
    private Map<String, Long> srStatusCounts;
    // Change stats
    private Map<String, Long> changeStatusCounts;
    // Inspection stats
    private Map<String, Long> inspectionStatusCounts;

    // Monitoring metrics
    private long unassignedIncidentCount;
    private long delayedIncidentCount;
    private long pendingSrCount;

    // Summary totals
    private long totalIncidentCount;
    private long totalSrCount;
    private long totalChangeCount;
    private long totalInspectionCount;

    // Monthly trend data (last 6 months)
    private List<MonthlyTrendItem> monthlyTrend;
}
