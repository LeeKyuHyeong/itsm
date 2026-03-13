package com.itsm.core.domain.asset;

import com.itsm.core.domain.company.Company;
import com.itsm.core.domain.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class AssetHwTest {

    @Test
    @DisplayName("Builder로 AssetHw 생성 시 기본값이 설정된다")
    void builder_createsAssetHwWithDefaults() {
        // given
        Company company = Company.builder().companyNm("테스트회사").bizNo("123-45-67890").build();
        ReflectionTestUtils.setField(company, "companyId", 1L);

        // when
        AssetHw asset = AssetHw.builder()
                .assetNm("서버#1")
                .assetTypeCd("SERVER")
                .manufacturer("Dell")
                .modelNm("PowerEdge R740")
                .serialNo("SN-001")
                .ipAddress("192.168.1.100")
                .macAddress("AA:BB:CC:DD:EE:FF")
                .location("IDC 1층")
                .introducedAt(LocalDate.of(2024, 1, 15))
                .warrantyEndAt(LocalDate.of(2027, 1, 15))
                .company(company)
                .description("메인 서버")
                .build();

        // then
        assertThat(asset.getAssetNm()).isEqualTo("서버#1");
        assertThat(asset.getAssetTypeCd()).isEqualTo("SERVER");
        assertThat(asset.getManufacturer()).isEqualTo("Dell");
        assertThat(asset.getModelNm()).isEqualTo("PowerEdge R740");
        assertThat(asset.getSerialNo()).isEqualTo("SN-001");
        assertThat(asset.getIpAddress()).isEqualTo("192.168.1.100");
        assertThat(asset.getMacAddress()).isEqualTo("AA:BB:CC:DD:EE:FF");
        assertThat(asset.getLocation()).isEqualTo("IDC 1층");
        assertThat(asset.getIntroducedAt()).isEqualTo(LocalDate.of(2024, 1, 15));
        assertThat(asset.getWarrantyEndAt()).isEqualTo(LocalDate.of(2027, 1, 15));
        assertThat(asset.getCompany()).isNotNull();
        assertThat(asset.getStatus()).isEqualTo("ACTIVE");
        assertThat(asset.getDescription()).isEqualTo("메인 서버");
        assertThat(asset.getAssetCategory()).isEqualTo("INFRA_HW");
    }

    @Test
    @DisplayName("Builder로 AssetHw 생성 시 assetCategory 미지정이면 INFRA_HW가 기본값이다")
    void builder_defaultAssetCategory() {
        // given
        Company company = Company.builder().companyNm("테스트회사").bizNo("123-45-67890").build();

        // when
        AssetHw asset = AssetHw.builder()
                .assetNm("서버#1")
                .assetTypeCd("SERVER")
                .company(company)
                .build();

        // then
        assertThat(asset.getAssetCategory()).isEqualTo("INFRA_HW");
        assertThat(asset.getAssetSubCategory()).isNull();
    }

    @Test
    @DisplayName("Builder로 AssetHw 생성 시 assetCategory를 지정하면 해당 값이 설정된다")
    void builder_customAssetCategory() {
        // given
        Company company = Company.builder().companyNm("테스트회사").bizNo("123-45-67890").build();

        // when
        AssetHw asset = AssetHw.builder()
                .assetNm("데스크톱#1")
                .assetTypeCd("DESKTOP")
                .assetCategory("OA")
                .assetSubCategory("OA_DESKTOP")
                .company(company)
                .build();

        // then
        assertThat(asset.getAssetCategory()).isEqualTo("OA");
        assertThat(asset.getAssetSubCategory()).isEqualTo("OA_DESKTOP");
    }

    @Test
    @DisplayName("update 호출 시 자산 정보가 변경된다")
    void update_changesAssetInfo() {
        // given
        Company company = Company.builder().companyNm("테스트회사").bizNo("123-45-67890").build();
        AssetHw asset = AssetHw.builder()
                .assetNm("서버#1")
                .assetTypeCd("SERVER")
                .company(company)
                .build();

        // when
        asset.update("서버#2", "NETWORK", "HP", "ProLiant DL380",
                "SN-002", "10.0.0.1", "11:22:33:44:55:66",
                "IDC 2층", LocalDate.of(2025, 6, 1), LocalDate.of(2028, 6, 1),
                null, "변경된 서버", "INFRA_HW", "NETWORK_SWITCH");

        // then
        assertThat(asset.getAssetNm()).isEqualTo("서버#2");
        assertThat(asset.getAssetTypeCd()).isEqualTo("NETWORK");
        assertThat(asset.getManufacturer()).isEqualTo("HP");
        assertThat(asset.getModelNm()).isEqualTo("ProLiant DL380");
        assertThat(asset.getSerialNo()).isEqualTo("SN-002");
        assertThat(asset.getIpAddress()).isEqualTo("10.0.0.1");
        assertThat(asset.getLocation()).isEqualTo("IDC 2층");
        assertThat(asset.getDescription()).isEqualTo("변경된 서버");
        assertThat(asset.getAssetCategory()).isEqualTo("INFRA_HW");
        assertThat(asset.getAssetSubCategory()).isEqualTo("NETWORK_SWITCH");
    }

    @Test
    @DisplayName("changeStatus로 상태를 변경한다")
    void changeStatus_changesStatus() {
        // given
        Company company = Company.builder().companyNm("테스트회사").bizNo("123-45-67890").build();
        AssetHw asset = AssetHw.builder()
                .assetNm("서버#1")
                .assetTypeCd("SERVER")
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
        AssetHw asset = AssetHw.builder()
                .assetNm("서버#1")
                .assetTypeCd("SERVER")
                .company(company)
                .build();
        User manager = User.builder().loginId("mgr01").password("pw").userNm("매니저").build();

        // when
        asset.assignManager(manager);

        // then
        assertThat(asset.getManager()).isEqualTo(manager);
    }
}
