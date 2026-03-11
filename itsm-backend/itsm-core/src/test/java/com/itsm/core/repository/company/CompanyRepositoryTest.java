package com.itsm.core.repository.company;

import com.itsm.core.TestJpaConfig;
import com.itsm.core.domain.company.Company;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(TestJpaConfig.class)
class CompanyRepositoryTest {

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private EntityManager entityManager;

    private Company savedCompany;

    @BeforeEach
    void setUp() {
        savedCompany = Company.builder()
                .companyNm("테스트회사")
                .bizNo("123-45-67890")
                .ceoNm("홍길동")
                .tel("02-1234-5678")
                .build();
        companyRepository.save(savedCompany);
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("save 후 findById로 조회하면 저장된 Company를 반환한다")
    void saveAndFindById() {
        // when
        Optional<Company> result = companyRepository.findById(savedCompany.getCompanyId());

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getCompanyNm()).isEqualTo("테스트회사");
        assertThat(result.get().getBizNo()).isEqualTo("123-45-67890");
        assertThat(result.get().getCeoNm()).isEqualTo("홍길동");
        assertThat(result.get().getTel()).isEqualTo("02-1234-5678");
        assertThat(result.get().getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("findByBizNo - 존재하는 사업자번호로 조회하면 Company를 반환한다")
    void findByBizNo_existingBizNo_returnsCompany() {
        // when
        Optional<Company> result = companyRepository.findByBizNo("123-45-67890");

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getCompanyNm()).isEqualTo("테스트회사");
    }

    @Test
    @DisplayName("findByBizNo - 존재하지 않는 사업자번호로 조회하면 빈 Optional을 반환한다")
    void findByBizNo_nonExistingBizNo_returnsEmpty() {
        // when
        Optional<Company> result = companyRepository.findByBizNo("999-99-99999");

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("existsByBizNo - 존재하는 사업자번호면 true를 반환한다")
    void existsByBizNo_existingBizNo_returnsTrue() {
        // when
        boolean exists = companyRepository.existsByBizNo("123-45-67890");

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsByBizNo - 존재하지 않는 사업자번호면 false를 반환한다")
    void existsByBizNo_nonExistingBizNo_returnsFalse() {
        // when
        boolean exists = companyRepository.existsByBizNo("999-99-99999");

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("search - 회사명으로 검색할 수 있다")
    void search_byCompanyNm_returnsMatchingCompanies() {
        // given
        Company anotherCompany = Company.builder()
                .companyNm("다른회사")
                .bizNo("987-65-43210")
                .ceoNm("이순신")
                .build();
        companyRepository.save(anotherCompany);
        entityManager.flush();
        entityManager.clear();

        // when
        Page<Company> result = companyRepository.search("테스트", PageRequest.of(0, 10));

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getCompanyNm()).isEqualTo("테스트회사");
    }

    @Test
    @DisplayName("search - 사업자번호로 검색할 수 있다")
    void search_byBizNo_returnsMatchingCompanies() {
        // when
        Page<Company> result = companyRepository.search("123-45", PageRequest.of(0, 10));

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getBizNo()).isEqualTo("123-45-67890");
    }

    @Test
    @DisplayName("search - 키워드가 없으면 전체 결과가 반환된다")
    void search_emptyKeyword_returnsAll() {
        // given
        Company anotherCompany = Company.builder()
                .companyNm("다른회사")
                .bizNo("987-65-43210")
                .build();
        companyRepository.save(anotherCompany);
        entityManager.flush();
        entityManager.clear();

        // when
        Page<Company> result = companyRepository.search("", PageRequest.of(0, 10));

        // then
        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("save - Company를 저장하면 ID가 자동 생성된다")
    void save_generatesId() {
        // given
        Company newCompany = Company.builder()
                .companyNm("신규회사")
                .bizNo("111-22-33333")
                .build();

        // when
        Company saved = companyRepository.save(newCompany);

        // then
        assertThat(saved.getCompanyId()).isNotNull();
    }

    @Test
    @DisplayName("save - Company 저장 시 기본 status는 ACTIVE이다")
    void save_defaultStatusIsActive() {
        // given
        Company newCompany = Company.builder()
                .companyNm("신규회사")
                .bizNo("111-22-33333")
                .build();

        // when
        Company saved = companyRepository.save(newCompany);

        // then
        assertThat(saved.getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("findByStatus - 특정 상태의 회사만 조회된다")
    void findByStatus_returnsMatchingCompanies() {
        // given
        Company inactiveCompany = Company.builder()
                .companyNm("비활성회사")
                .bizNo("555-66-77777")
                .status("INACTIVE")
                .build();
        companyRepository.save(inactiveCompany);
        entityManager.flush();
        entityManager.clear();

        // when
        Page<Company> activeResult = companyRepository.findByStatus("ACTIVE", PageRequest.of(0, 10));
        Page<Company> inactiveResult = companyRepository.findByStatus("INACTIVE", PageRequest.of(0, 10));

        // then
        assertThat(activeResult.getContent()).hasSize(1);
        assertThat(activeResult.getContent().get(0).getCompanyNm()).isEqualTo("테스트회사");
        assertThat(inactiveResult.getContent()).hasSize(1);
        assertThat(inactiveResult.getContent().get(0).getCompanyNm()).isEqualTo("비활성회사");
    }
}
