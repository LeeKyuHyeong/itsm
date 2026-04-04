package com.itsm.core.constant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StatusConstantsTest {

    @Test
    @DisplayName("UserStatus 상수가 올바른 값을 갖는다")
    void userStatus_hasCorrectValues() {
        assertThat(UserStatus.ACTIVE).isEqualTo("ACTIVE");
        assertThat(UserStatus.LOCKED).isEqualTo("LOCKED");
        assertThat(UserStatus.DELETED).isEqualTo("DELETED");
    }

    @Test
    @DisplayName("IncidentStatus 상수가 올바른 값을 갖는다")
    void incidentStatus_hasCorrectValues() {
        assertThat(IncidentStatus.RECEIVED).isEqualTo("RECEIVED");
        assertThat(IncidentStatus.IN_PROGRESS).isEqualTo("IN_PROGRESS");
        assertThat(IncidentStatus.COMPLETED).isEqualTo("COMPLETED");
        assertThat(IncidentStatus.CLOSED).isEqualTo("CLOSED");
        assertThat(IncidentStatus.REJECTED).isEqualTo("REJECTED");
    }

    @Test
    @DisplayName("ServiceRequestStatus 상수가 올바른 값을 갖는다")
    void srStatus_hasCorrectValues() {
        assertThat(ServiceRequestStatus.RECEIVED).isEqualTo("RECEIVED");
        assertThat(ServiceRequestStatus.ASSIGNED).isEqualTo("ASSIGNED");
        assertThat(ServiceRequestStatus.IN_PROGRESS).isEqualTo("IN_PROGRESS");
        assertThat(ServiceRequestStatus.PENDING_COMPLETE).isEqualTo("PENDING_COMPLETE");
        assertThat(ServiceRequestStatus.CLOSED).isEqualTo("CLOSED");
        assertThat(ServiceRequestStatus.CANCELLED).isEqualTo("CANCELLED");
        assertThat(ServiceRequestStatus.REJECTED).isEqualTo("REJECTED");
    }

    @Test
    @DisplayName("RoleCode 상수가 올바른 값을 갖는다")
    void roleCode_hasCorrectValues() {
        assertThat(RoleCode.SUPER_ADMIN).isEqualTo("SUPER_ADMIN");
        assertThat(RoleCode.ITSM_ADMIN).isEqualTo("ITSM_ADMIN");
        assertThat(RoleCode.HAS_ADMIN_ROLE).isEqualTo("hasRole('SUPER_ADMIN') or hasRole('ITSM_ADMIN')");
    }
}
