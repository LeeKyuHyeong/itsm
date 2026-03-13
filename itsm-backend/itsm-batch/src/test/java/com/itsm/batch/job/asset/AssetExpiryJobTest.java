package com.itsm.batch.job.asset;

import com.itsm.batch.service.NotificationService;
import com.itsm.core.domain.asset.AssetHw;
import com.itsm.core.repository.asset.AssetHwRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssetExpiryJobTest {

    @Mock
    private AssetHwRepository assetHwRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private AssetExpiryJob assetExpiryJob;

    @Test
    @DisplayName("보증 만료 임박 자산에 대해 알림을 발송한다")
    void execute_sendsNotificationForExpiringAssets() {
        // given
        AssetHw asset = mock(AssetHw.class);
        when(asset.getAssetHwId()).thenReturn(100L);
        when(asset.getAssetNm()).thenReturn("서버-01");
        when(asset.getSerialNo()).thenReturn("SN12345");
        when(asset.getWarrantyEndAt()).thenReturn(LocalDate.now().plusDays(5));

        when(assetHwRepository.findByWarrantyEndAtBetween(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(asset));

        // when
        assetExpiryJob.execute();

        // then
        verify(notificationService).sendNotification(
                eq(1L),
                eq("ASSET_EXPIRY"),
                contains("긴급"),
                anyString(),
                eq("ASSET_HW"),
                eq(100L)
        );
    }

    @Test
    @DisplayName("보증 만료 30일 이내 자산은 일반 알림으로 발송한다")
    void execute_sendsNormalAlertForAssetExpiring30Days() {
        // given
        AssetHw asset = mock(AssetHw.class);
        when(asset.getAssetHwId()).thenReturn(200L);
        when(asset.getAssetNm()).thenReturn("스위치-02");
        when(asset.getSerialNo()).thenReturn("SN67890");
        when(asset.getWarrantyEndAt()).thenReturn(LocalDate.now().plusDays(20));

        when(assetHwRepository.findByWarrantyEndAtBetween(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(asset));

        // when
        assetExpiryJob.execute();

        // then
        verify(notificationService).sendNotification(
                eq(1L),
                eq("ASSET_EXPIRY"),
                contains("알림"),
                anyString(),
                eq("ASSET_HW"),
                eq(200L)
        );
    }

    @Test
    @DisplayName("만료 임박 자산이 없으면 알림을 발송하지 않는다")
    void execute_doesNotSendWhenNoExpiringAssets() {
        // given
        when(assetHwRepository.findByWarrantyEndAtBetween(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        // when
        assetExpiryJob.execute();

        // then
        verify(notificationService, never()).sendNotification(
                anyLong(), anyString(), anyString(), anyString(), anyString(), anyLong());
    }
}
