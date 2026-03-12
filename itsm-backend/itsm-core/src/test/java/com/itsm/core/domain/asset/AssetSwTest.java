package com.itsm.core.domain.asset;

import com.itsm.core.domain.company.Company;
import com.itsm.core.domain.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class AssetSwTest {

    @Test
    @DisplayName("Builder로 AssetSw 생성 시 기본값이 설정된다")
    void builder_createsAssetSwWithDefaults() {
        // given
        Company company = Company.builder().companyNm("테스트회사").bizNo("123-45-67890").build();
        ReflectionTestUtils.setField(company, "companyId", 1L);

        // when
        AssetSw sw = AssetSw.builder()
                .swNm("Oracle DB")
                .swTypeCd("DATABASE")
                .version("19c")
                .licenseKey("ORACLE-LIC-001")
                .licenseCnt(10)
                .installedAt(LocalDate.of(2024, 3, 1))
                .expiredAt(LocalDate.of(2026, 3, 1))
                .company(company)
                .description("메인 DB")
                .build();

        // then
        assertThat(sw.getSwNm()).isEqualTo("Oracle DB");
        assertThat(sw.getSwTypeCd()).isEqualTo("DATABASE");
        assertThat(sw.getVersion()).isEqualTo("19c");
        assertThat(sw.getLicenseKey()).isEqualTo("ORACLE-LIC-001");
        assertThat(sw.getLicenseCnt()).isEqualTo(10);
        assertThat(sw.getInstalledAt()).isEqualTo(LocalDate.of(2024, 3, 1));
        assertThat(sw.getExpiredAt()).isEqualTo(LocalDate.of(2026, 3, 1));
        assertThat(sw.getCompany()).isNotNull();
        assertThat(sw.getStatus()).isEqualTo("ACTIVE");
        assertThat(sw.getDescription()).isEqualTo("메인 DB");
    }

    @Test
    @DisplayName("update 호출 시 SW 자산 정보가 변경된다")
    void update_changesSwInfo() {
        // given
        Company company = Company.builder().companyNm("테스트회사").bizNo("123-45-67890").build();
        AssetSw sw = AssetSw.builder()
                .swNm("Oracle DB")
                .swTypeCd("DATABASE")
                .company(company)
                .build();

        // when
        sw.update("MySQL", "DATABASE", "8.0", "MYSQL-LIC-001", 5,
                LocalDate.of(2025, 1, 1), LocalDate.of(2027, 1, 1),
                null, "변경됨");

        // then
        assertThat(sw.getSwNm()).isEqualTo("MySQL");
        assertThat(sw.getVersion()).isEqualTo("8.0");
        assertThat(sw.getLicenseKey()).isEqualTo("MYSQL-LIC-001");
        assertThat(sw.getLicenseCnt()).isEqualTo(5);
        assertThat(sw.getDescription()).isEqualTo("변경됨");
    }

    @Test
    @DisplayName("changeStatus로 상태를 변경한다")
    void changeStatus_changesStatus() {
        // given
        Company company = Company.builder().companyNm("테스트회사").bizNo("123-45-67890").build();
        AssetSw sw = AssetSw.builder()
                .swNm("Oracle DB")
                .swTypeCd("DATABASE")
                .company(company)
                .build();

        // when
        sw.changeStatus("INACTIVE");

        // then
        assertThat(sw.getStatus()).isEqualTo("INACTIVE");
    }

    @Test
    @DisplayName("assignManager로 담당자를 지정한다")
    void assignManager_setsManager() {
        // given
        Company company = Company.builder().companyNm("테스트회사").bizNo("123-45-67890").build();
        AssetSw sw = AssetSw.builder()
                .swNm("Oracle DB")
                .swTypeCd("DATABASE")
                .company(company)
                .build();
        User manager = User.builder().loginId("mgr01").password("pw").userNm("매니저").build();

        // when
        sw.assignManager(manager);

        // then
        assertThat(sw.getManager()).isEqualTo(manager);
    }
}
