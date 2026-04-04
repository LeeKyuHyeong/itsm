package com.itsm.core.domain;

import com.itsm.core.domain.asset.AssetHw;
import com.itsm.core.domain.asset.AssetOa;
import com.itsm.core.domain.asset.AssetSw;
import com.itsm.core.domain.change.Change;
import com.itsm.core.domain.incident.Incident;
import com.itsm.core.domain.inspection.Inspection;
import com.itsm.core.domain.servicerequest.ServiceRequest;
import com.itsm.core.domain.user.User;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class EntityIndexTest {

    private Set<String> getIndexColumnNames(Class<?> entityClass) {
        Table table = entityClass.getAnnotation(Table.class);
        assertThat(table).isNotNull();
        return Arrays.stream(table.indexes())
                .map(Index::columnList)
                .collect(Collectors.toSet());
    }

    @Test
    @DisplayName("User 엔티티에 login_id, status, dept_id 인덱스가 있다")
    void user_hasRequiredIndexes() {
        Set<String> columns = getIndexColumnNames(User.class);
        assertThat(columns).contains("login_id", "status", "dept_id");
    }

    @Test
    @DisplayName("Incident 엔티티에 status_cd, sla_deadline_at, created_at 인덱스가 있다")
    void incident_hasRequiredIndexes() {
        Set<String> columns = getIndexColumnNames(Incident.class);
        assertThat(columns).contains("status_cd", "sla_deadline_at", "created_at");
    }

    @Test
    @DisplayName("ServiceRequest 엔티티에 status_cd, created_at 인덱스가 있다")
    void serviceRequest_hasRequiredIndexes() {
        Set<String> columns = getIndexColumnNames(ServiceRequest.class);
        assertThat(columns).contains("status_cd", "created_at");
    }

    @Test
    @DisplayName("Change 엔티티에 status_cd, created_at 인덱스가 있다")
    void change_hasRequiredIndexes() {
        Set<String> columns = getIndexColumnNames(Change.class);
        assertThat(columns).contains("status_cd", "created_at");
    }

    @Test
    @DisplayName("Inspection 엔티티에 status_cd 인덱스가 있다")
    void inspection_hasRequiredIndexes() {
        Set<String> columns = getIndexColumnNames(Inspection.class);
        assertThat(columns).contains("status_cd");
    }

    @Test
    @DisplayName("AssetHw 엔티티에 status, asset_type_cd, company_id 인덱스가 있다")
    void assetHw_hasRequiredIndexes() {
        Set<String> columns = getIndexColumnNames(AssetHw.class);
        assertThat(columns).contains("status", "asset_type_cd", "company_id");
    }

    @Test
    @DisplayName("AssetSw 엔티티에 status, sw_type_cd, company_id 인덱스가 있다")
    void assetSw_hasRequiredIndexes() {
        Set<String> columns = getIndexColumnNames(AssetSw.class);
        assertThat(columns).contains("status", "sw_type_cd", "company_id");
    }

    @Test
    @DisplayName("AssetOa 엔티티에 status, asset_type_cd, company_id 인덱스가 있다")
    void assetOa_hasRequiredIndexes() {
        Set<String> columns = getIndexColumnNames(AssetOa.class);
        assertThat(columns).contains("status", "asset_type_cd", "company_id");
    }
}
