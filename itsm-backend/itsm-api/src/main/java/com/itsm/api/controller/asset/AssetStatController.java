package com.itsm.api.controller.asset;

import com.itsm.api.dto.asset.AssetStatResponse;
import com.itsm.api.service.asset.AssetStatService;
import com.itsm.core.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/assets/stats")
@RequiredArgsConstructor
public class AssetStatController {

    private final AssetStatService assetStatService;

    @GetMapping
    public ApiResponse<AssetStatResponse> getStats() {
        return ApiResponse.success(assetStatService.getStats());
    }
}
