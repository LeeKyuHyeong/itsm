package com.itsm.api.service.asset;

import com.itsm.api.dto.asset.AssetStatResponse;
import com.itsm.core.repository.asset.AssetHwRepository;
import com.itsm.core.repository.asset.AssetSwRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AssetStatService {

    private final AssetHwRepository assetHwRepository;
    private final AssetSwRepository assetSwRepository;

    public AssetStatResponse getStats() {
        Map<String, Long> categoryCounts = new LinkedHashMap<>();
        Map<String, Map<String, Long>> subCategoryCounts = new LinkedHashMap<>();
        Map<String, Long> statusCounts = new LinkedHashMap<>();

        // HW category counts
        for (Object[] row : assetHwRepository.countByCategory()) {
            String cat = (String) row[0];
            Long cnt = (Long) row[1];
            categoryCounts.merge(cat != null ? cat : "UNKNOWN", cnt, Long::sum);
        }
        // SW category counts
        for (Object[] row : assetSwRepository.countByCategory()) {
            String cat = (String) row[0];
            Long cnt = (Long) row[1];
            categoryCounts.merge(cat != null ? cat : "UNKNOWN", cnt, Long::sum);
        }

        // Sub-category counts per category
        for (String category : List.of("INFRA_HW", "OA")) {
            Map<String, Long> subMap = new LinkedHashMap<>();
            for (Object[] row : assetHwRepository.countBySubCategory(category)) {
                subMap.put(row[0] != null ? (String) row[0] : "UNKNOWN", (Long) row[1]);
            }
            if (!subMap.isEmpty()) subCategoryCounts.put(category, subMap);
        }
        Map<String, Long> swSubMap = new LinkedHashMap<>();
        for (Object[] row : assetSwRepository.countBySubCategory("INFRA_SW")) {
            swSubMap.put(row[0] != null ? (String) row[0] : "UNKNOWN", (Long) row[1]);
        }
        if (!swSubMap.isEmpty()) subCategoryCounts.put("INFRA_SW", swSubMap);

        // Status counts (combine HW + SW)
        for (Object[] row : assetHwRepository.countByStatus()) {
            statusCounts.merge((String) row[0], (Long) row[1], Long::sum);
        }
        for (Object[] row : assetSwRepository.countByStatus()) {
            statusCounts.merge((String) row[0], (Long) row[1], Long::sum);
        }

        return AssetStatResponse.builder()
                .categoryCounts(categoryCounts)
                .subCategoryCounts(subCategoryCounts)
                .statusCounts(statusCounts)
                .build();
    }
}
