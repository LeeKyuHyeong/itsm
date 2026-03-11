package com.itsm.core.repository.user;

import com.itsm.core.TestJpaConfig;
import com.itsm.core.domain.company.Company;
import com.itsm.core.domain.company.Department;
import com.itsm.core.domain.user.User;
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
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    private User savedUser;

    @BeforeEach
    void setUp() {
        Company company = Company.builder()
                .companyNm("테스트회사")
                .bizNo("123-45-67890")
                .ceoNm("홍길동")
                .build();
        entityManager.persist(company);

        Department department = Department.builder()
                .deptNm("개발팀")
                .company(company)
                .build();
        entityManager.persist(department);

        savedUser = User.builder()
                .loginId("testuser")
                .password("encoded_password")
                .userNm("테스트유저")
                .employeeNo("EMP001")
                .department(department)
                .email("test@example.com")
                .tel("010-1234-5678")
                .build();
        userRepository.save(savedUser);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("findByLoginId - 존재하는 loginId로 조회하면 User를 반환한다")
    void findByLoginId_existingLoginId_returnsUser() {
        // when
        Optional<User> result = userRepository.findByLoginId("testuser");

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getLoginId()).isEqualTo("testuser");
        assertThat(result.get().getUserNm()).isEqualTo("테스트유저");
    }

    @Test
    @DisplayName("findByLoginId - 존재하지 않는 loginId로 조회하면 빈 Optional을 반환한다")
    void findByLoginId_nonExistingLoginId_returnsEmpty() {
        // when
        Optional<User> result = userRepository.findByLoginId("nonexistent");

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("existsByLoginId - 존재하는 loginId면 true를 반환한다")
    void existsByLoginId_existingLoginId_returnsTrue() {
        // when
        boolean exists = userRepository.existsByLoginId("testuser");

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsByLoginId - 존재하지 않는 loginId면 false를 반환한다")
    void existsByLoginId_nonExistingLoginId_returnsFalse() {
        // when
        boolean exists = userRepository.existsByLoginId("nonexistent");

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("findAllActive - DELETED 상태의 사용자는 제외된다")
    void findAllActive_excludesDeletedUsers() {
        // given
        User activeUser = User.builder()
                .loginId("activeuser")
                .password("pw")
                .userNm("활성유저")
                .build();
        userRepository.save(activeUser);

        User deletedUser = User.builder()
                .loginId("deleteduser")
                .password("pw")
                .userNm("삭제유저")
                .status("DELETED")
                .build();
        userRepository.save(deletedUser);

        entityManager.flush();
        entityManager.clear();

        // when
        Page<User> result = userRepository.findAllActive(PageRequest.of(0, 10));

        // then
        assertThat(result.getContent()).hasSize(2); // testuser + activeuser
        assertThat(result.getContent())
                .extracting(User::getLoginId)
                .containsExactlyInAnyOrder("testuser", "activeuser");
        assertThat(result.getContent())
                .extracting(User::getLoginId)
                .doesNotContain("deleteduser");
    }

    @Test
    @DisplayName("search - 키워드로 사용자명을 검색할 수 있다")
    void search_byUserNm_returnsMatchingUsers() {
        // given
        User anotherUser = User.builder()
                .loginId("another")
                .password("pw")
                .userNm("다른유저")
                .email("another@example.com")
                .build();
        userRepository.save(anotherUser);
        entityManager.flush();
        entityManager.clear();

        // when
        Page<User> result = userRepository.search("테스트", PageRequest.of(0, 10));

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUserNm()).isEqualTo("테스트유저");
    }

    @Test
    @DisplayName("search - 키워드로 loginId를 검색할 수 있다")
    void search_byLoginId_returnsMatchingUsers() {
        // when
        Page<User> result = userRepository.search("testuser", PageRequest.of(0, 10));

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getLoginId()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("search - 키워드로 이메일을 검색할 수 있다")
    void search_byEmail_returnsMatchingUsers() {
        // when
        Page<User> result = userRepository.search("test@example", PageRequest.of(0, 10));

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("search - DELETED 상태의 사용자는 검색 결과에서 제외된다")
    void search_excludesDeletedUsers() {
        // given
        User deletedUser = User.builder()
                .loginId("deleted_search")
                .password("pw")
                .userNm("테스트삭제유저")
                .status("DELETED")
                .build();
        userRepository.save(deletedUser);
        entityManager.flush();
        entityManager.clear();

        // when
        Page<User> result = userRepository.search("테스트", PageRequest.of(0, 10));

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getStatus()).isNotEqualTo("DELETED");
    }

    @Test
    @DisplayName("save - User를 저장하면 ID가 자동 생성된다")
    void save_generatesId() {
        // given
        User newUser = User.builder()
                .loginId("newuser")
                .password("pw")
                .userNm("신규유저")
                .build();

        // when
        User saved = userRepository.save(newUser);

        // then
        assertThat(saved.getUserId()).isNotNull();
    }
}
