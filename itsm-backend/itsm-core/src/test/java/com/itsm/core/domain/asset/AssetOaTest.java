package com.itsm.core.domain.asset;

import com.itsm.core.domain.company.Company;
import com.itsm.core.domain.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class AssetOaTest {

    @Test
    @DisplayName("Builder로 AssetOa 생성 시 기본값이 설정된다")
    void builder_createsAssetOaWithDefaults() {
        // given
        Company company = Company.builder().companyNm("테스트회사").bizNo("123-45-67890").build();
        ReflectionTestUtils.setField(company, "companyId", 1L);

        // when
        AssetOa asset = AssetOa.builder()
                .assetNm("프린터#1")
                .assetTypeCd("PRINTER")
                .manufacturer("HP")
                .modelNm("LaserJet Pro")
                .serialNo("OA-SN-001")
                .ipAddress("192.168.10.50")
                .macAddress("AA:BB:CC:DD:EE:01")
                .location("3층 사무실")
                .introducedAt(LocalDate.of(2024, 3, 1))
                .warrantyEndAt(LocalDate.of(2027, 3, 1))
                .company(company)
                .description("사무실 프린터")
                .build();

        // then
        assertThat(asset.getAssetNm()).isEqualTo("프린터#1");
        assertThat(asset.getAssetTypeCd()).isEqualTo("PRINTER");
        assertThat(asset.getManufacturer()).isEqualTo("HP");
        assertThat(asset.getModelNm()).isEqualTo("LaserJet Pro");
        assertThat(asset.getSerialNo()).isEqualTo("OA-SN-001");
        assertThat(asset.getIpAddress()).isEqualTo("192.168.10.50");
        assertThat(asset.getMacAddress()).isEqualTo("AA:BB:CC:DD:EE:01");
        assertThat(asset.getLocation()).isEqualTo("3층 사무실");
        assertThat(asset.getIntroducedAt()).isEqualTo(LocalDate.of(2024, 3, 1));
        assertThat(asset.getWarrantyEndAt()).isEqualTo(LocalDate.of(2027, 3, 1));
        assertThat(asset.getCompany()).isNotNull();
        assertThat(asset.getStatus()).isEqualTo("ACTIVE");
        assertThat(asset.getDescription()).isEqualTo("사무실 프린터");
    }

    @Test
    @DisplayName("update 호출 시 OA 자산 정보가 변경된다")
    void update_changesAssetInfo() {
        // given
        Company company = Company.builder().companyNm("테스트회사").bizNo("123-45-67890").build();
        AssetOa asset = AssetOa.builder()
                .assetNm("프린터#1")
                .assetTypeCd("PRINTER")
                .company(company)
                .build();

        // when
        asset.update("복합기#1", "MFP", "Canon", "imageRUNNER",
                "OA-SN-002", "192.168.10.60", "BB:CC:DD:EE:FF:02",
                "2층 사무실", LocalDate.of(2025, 6, 1), LocalDate.of(2028, 6, 1),
                null, "변경된 복합기");

        // then
        assertThat(asset.getAssetNm()).isEqualTo("복합기#1");
        assertThat(asset.getAssetTypeCd()).isEqualTo("MFP");
        assertThat(asset.getManufacturer()).isEqualTo("Canon");
        assertThat(asset.getModelNm()).isEqualTo("imageRUNNER");
        assertThat(asset.getSerialNo()).isEqualTo("OA-SN-002");
        assertThat(asset.getLocation()).isEqualTo("2층 사무실");
        assertThat(asset.getDescription()).isEqualTo("변경된 복합기");
    }

    @Test
    @DisplayName("changeStatus로 상태를 변경한다")
    void changeStatus_changesStatus() {
        // given
        Company company = Company.builder().companyNm("테스트회사").bizNo("123-45-67890").build();
        AssetOa asset = AssetOa.builder()
                .assetNm("프린터#1")
                .assetTypeCd("PRINTER")
                .company(company)
                .build();
        assertThat(asset.getStatus()).isEqualTo("ACTIVE");

        // when
        asset.changeStatus("DISPOSED");

        // then
        assertThat(asset.getStatus()).isEqualTo("DISPOSED");
    }

    @Test
    @DisplayName("assignManager로 담당자를 지정한다")
    void assignManager_setsManager() {
        // given
        Company company = Company.builder().companyNm("테스트회사").bizNo("123-45-67890").build();
        AssetOa asset = AssetOa.builder()
                .assetNm("프린터#1")
                .assetTypeCd("PRINTER")
                .company(company)
                .build();
        User manager = User.builder().loginId("mgr01").password("pw").userNm("매니저").build();

        // when
        asset.assignManager(manager);

        // then
        assertThat(asset.getManager()).isEqualTo(manager);
    }
}
