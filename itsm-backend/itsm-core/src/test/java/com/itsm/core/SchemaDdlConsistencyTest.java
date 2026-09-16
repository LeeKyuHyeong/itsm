package com.itsm.core;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Table;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 2026-09-16 전수조사 P6 — 운영은 ddl-auto=validate 인데 리포의 sql/01_ddl.sql 이 엔티티와 어긋나 있었다:
 * tb_login_history·tb_sim_menu_access_log·tb_daily_statistics 는 CREATE 가 없고, tb_service_request 는
 * phase26 컬럼 5개가 별도 마이그레이션에만 있었다. 즉 "sql/01~03 으로 새로 설치하면 기동이 실패" 했다(career T-17 일반화).
 * 이 테스트는 모든 @Entity 의 @Table 과 @Column/@JoinColumn 이름이 01_ddl.sql 에 존재함을 고정한다.
 */
class SchemaDdlConsistencyTest {

    private static final Pattern CREATE = Pattern.compile(
            "CREATE TABLE IF NOT EXISTS (\\w+)\\s*\\((.*?)\\n\\)\\s*ENGINE", Pattern.DOTALL);
    private static final Pattern COLUMN_LINE = Pattern.compile("^\\s*(\\w+)\\s+[A-Z]+", Pattern.MULTILINE);
    private static final Set<String> KEYWORDS = Set.of("PRIMARY", "UNIQUE", "INDEX", "CONSTRAINT", "KEY", "FOREIGN");

    private static Map<String, Set<String>> ddlTables() throws Exception {
        Path ddl = Path.of("..", "..", "sql", "01_ddl.sql").toAbsolutePath().normalize();
        assertThat(ddl).exists();
        String sql = Files.readString(ddl, StandardCharsets.UTF_8);
        Map<String, Set<String>> tables = new HashMap<>();
        Matcher m = CREATE.matcher(sql);
        while (m.find()) {
            Set<String> cols = new LinkedHashSet<>();
            Matcher c = COLUMN_LINE.matcher(m.group(2));
            while (c.find()) {
                if (!KEYWORDS.contains(c.group(1).toUpperCase())) {
                    cols.add(c.group(1));
                }
            }
            tables.put(m.group(1), cols);
        }
        return tables;
    }

    private static List<Class<?>> entityClasses() throws Exception {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Entity.class));
        List<Class<?>> classes = new ArrayList<>();
        for (var bd : scanner.findCandidateComponents("com.itsm.core.domain")) {
            classes.add(Class.forName(bd.getBeanClassName()));
        }
        return classes;
    }

    /** 엔티티(상위 @MappedSuperclass 포함)가 매핑하는 컬럼명 */
    private static Set<String> mappedColumns(Class<?> type) {
        Set<String> cols = new LinkedHashSet<>();
        for (Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass()) {
            if (c != type && !c.isAnnotationPresent(MappedSuperclass.class)) {
                continue;
            }
            for (Field f : c.getDeclaredFields()) {
                Column col = f.getAnnotation(Column.class);
                JoinColumn join = f.getAnnotation(JoinColumn.class);
                if (col != null) {
                    cols.add(col.name().isEmpty() ? f.getName() : col.name());
                } else if (join != null) {
                    cols.add(join.name());
                }
            }
        }
        return cols;
    }

    @Test
    @DisplayName("모든 @Entity 의 @Table 이 sql/01_ddl.sql 에 CREATE TABLE 로 존재한다")
    void everyEntityTableHasDdl() throws Exception {
        Map<String, Set<String>> tables = ddlTables();
        List<String> missing = new ArrayList<>();
        for (Class<?> e : entityClasses()) {
            Table t = e.getAnnotation(Table.class);
            if (t != null && !tables.containsKey(t.name())) {
                missing.add(e.getSimpleName() + " → " + t.name());
            }
        }
        assertThat(missing).as("DDL 에 없는 엔티티 테이블 (validate 기동 실패)").isEmpty();
    }

    @Test
    @DisplayName("모든 @Column/@JoinColumn 이름이 해당 테이블의 DDL 에 존재한다")
    void everyMappedColumnExistsInDdl() throws Exception {
        Map<String, Set<String>> tables = ddlTables();
        List<String> drift = new ArrayList<>();
        for (Class<?> e : entityClasses()) {
            Table t = e.getAnnotation(Table.class);
            if (t == null || !tables.containsKey(t.name())) {
                continue; // 테이블 부재는 위 테스트가 잡는다
            }
            Set<String> ddlCols = tables.get(t.name());
            for (String col : mappedColumns(e)) {
                if (!ddlCols.contains(col)) {
                    drift.add(t.name() + "." + col + " (" + e.getSimpleName() + ")");
                }
            }
        }
        assertThat(drift).as("엔티티에는 있고 DDL 에는 없는 컬럼").isEmpty();
    }
}
