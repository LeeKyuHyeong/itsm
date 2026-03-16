package com.itsm.api.service.asset;

import com.itsm.api.dto.asset.AssetStatResponse;
import com.itsm.core.repository.asset.AssetHwRepository;
import com.itsm.core.repository.asset.AssetOaRepository;
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
    private final AssetOaRepository assetOaRepository;

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
        // OA counts (별도 테이블)
        long oaTotal = assetOaRepository.count();
        if (oaTotal > 0) {
            categoryCounts.put("OA", oaTotal);
        }

        // Sub-category counts per category
        Map<String, Long> hwSubMap = new LinkedHashMap<>();
        for (Object[] row : assetHwRepository.countBySubCategory("INFRA_HW")) {
            hwSubMap.put(row[0] != null ? (String) row[0] : "UNKNOWN", (Long) row[1]);
        }
        if (!hwSubMap.isEmpty()) subCategoryCounts.put("INFRA_HW", hwSubMap);

        Map<String, Long> swSubMap = new LinkedHashMap<>();
        for (Object[] row : assetSwRepository.countBySubCategory("INFRA_SW")) {
            swSubMap.put(row[0] != null ? (String) row[0] : "UNKNOWN", (Long) row[1]);
        }
        if (!swSubMap.isEmpty()) subCategoryCounts.put("INFRA_SW", swSubMap);

        // OA sub-category = assetTypeCd 기반
        Map<String, Long> oaSubMap = new LinkedHashMap<>();
        for (Object[] row : assetOaRepository.countByTypeCd()) {
            oaSubMap.put(row[0] != null ? (String) row[0] : "UNKNOWN", (Long) row[1]);
        }
        if (!oaSubMap.isEmpty()) subCategoryCounts.put("OA", oaSubMap);

        // Status counts (combine HW + SW + OA)
        for (Object[] row : assetHwRepository.countByStatus()) {
            statusCounts.merge((String) row[0], (Long) row[1], Long::sum);
        }
        for (Object[] row : assetSwRepository.countByStatus()) {
            statusCounts.merge((String) row[0], (Long) row[1], Long::sum);
        }
        for (Object[] row : assetOaRepository.countByStatus()) {
            statusCounts.merge((String) row[0], (Long) row[1], Long::sum);
        }

        return AssetStatResponse.builder()
                .categoryCounts(categoryCounts)
                .subCategoryCounts(subCategoryCounts)
                .statusCounts(statusCounts)
                .build();
    }
}
