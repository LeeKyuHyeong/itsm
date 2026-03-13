package com.itsm.batch.job.asset;

import com.itsm.core.domain.company.Company;
import com.itsm.core.domain.user.User;
import com.itsm.core.repository.asset.AssetHwRepository;
import com.itsm.core.repository.asset.AssetSwRepository;
import com.itsm.core.repository.company.CompanyRepository;
import com.itsm.core.repository.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("AssetAutoRegisterJob 테스트")
class AssetAutoRegisterJobTest {

    @Mock
    private AssetHwRepository assetHwRepository;

    @Mock
    private AssetSwRepository assetSwRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AssetAutoRegisterJob assetAutoRegisterJob;

    @Test
    @DisplayName("회사 데이터가 없으면 자산 등록을 건너뛴다")
    void execute_noCompany_skip() {
        // given
        when(companyRepository.findAll()).thenReturn(Collections.emptyList());

        // when
        assetAutoRegisterJob.execute();

        // then
        verify(assetHwRepository, never()).save(any());
        verify(assetSwRepository, never()).save(any());
    }

    @Test
    @DisplayName("사용자 데이터가 없으면 자산 등록을 건너뛴다")
    void execute_noUser_skip() {
        // given
        Company company = mock(Company.class);
        when(companyRepository.findAll()).thenReturn(List.of(company));
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        // when
        assetAutoRegisterJob.execute();

        // then
        verify(assetHwRepository, never()).save(any());
        verify(assetSwRepository, never()).save(any());
    }

    @Test
    @DisplayName("HW 자산이 50개 미만이면 초기 시딩을 수행한다 - HW 300건 이상, SW 50건 이상 생성")
    void execute_initialSeeding() {
        // given
        Company company = mock(Company.class);
        User user = mock(User.class);

        when(companyRepository.findAll()).thenReturn(List.of(company));
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(assetHwRepository.count()).thenReturn(0L);

        // when
        assetAutoRegisterJob.execute();

        // then: 운영장비 100+ + OA자산 200+ = HW 300건 이상
        verify(assetHwRepository, atLeast(300)).save(any());
        // then: 운영SW 50건 이상
        verify(assetSwRepository, atLeast(50)).save(any());
    }

    @Test
    @DisplayName("HW 자산이 50개 이상이면 월간 1-2개만 추가한다")
    void execute_monthlyAddition() {
        // given
        Company company = mock(Company.class);
        User user = mock(User.class);

        when(companyRepository.findAll()).thenReturn(List.of(company));
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(assetHwRepository.count()).thenReturn(300L);

        // when
        assetAutoRegisterJob.execute();

        // then: HW 1-2개만 추가
        verify(assetHwRepository, atMost(2)).save(any());
        verify(assetSwRepository, atMost(1)).save(any());
    }
}
