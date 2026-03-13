package com.itsm.api.dto.asset;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetStatResponse {
    private Map<String, Long> categoryCounts;
    private Map<String, Map<String, Long>> subCategoryCounts;
    private Map<String, Long> statusCounts;
}
