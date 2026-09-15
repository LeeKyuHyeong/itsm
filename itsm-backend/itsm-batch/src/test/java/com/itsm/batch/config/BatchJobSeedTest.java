package com.itsm.batch.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 2026-09-16 전수조사 P4 — DynamicScheduler 는 tb_batch_job.job_name 을 빈 이름으로 바꿔(첫 글자 소문자) 잡을 찾는다.
 * 그런데 sql/ 에 tb_batch_job 의 DDL 도 시드도 없어(career T-17) 새 설치에서는 배치가 0개 등록됐고,
 * 운영 행의 job_name 이 클래스명과 다르면 "배치 등록 실패" 로그만 남고 관리 화면엔 아무 표시가 없었다.
 * 이 테스트는 (1) 시드가 존재하고 (2) 시드의 job_name 이 전부 실제 잡 클래스와 1:1 로 맞는지 고정한다.
 */
class BatchJobSeedTest {

    private static final Pattern JOB_NAME = Pattern.compile("^\\('([A-Za-z]+Job)',", Pattern.MULTILINE);

    private static Path repoSql(String name) {
        // 모듈 작업 디렉터리(itsm-backend/itsm-batch) 기준 리포 루트의 sql/
        Path p = Path.of("..", "..", "sql", name).toAbsolutePath().normalize();
        assertThat(p).as("리포 sql 파일 %s", p).exists();
        return p;
    }

    private static Set<String> seededJobNames(String sql) {
        int start = sql.indexOf("INSERT INTO tb_batch_job");
        assertThat(start).as("02_dml.sql 에 tb_batch_job 시드가 있어야 한다").isGreaterThanOrEqualTo(0);
        String block = sql.substring(start, sql.indexOf(";", start));
        Matcher m = JOB_NAME.matcher(block);
        Set<String> names = new TreeSet<>();
        while (m.find()) {
            names.add(m.group(1));
        }
        return names;
    }

    private static Set<String> jobClassSimpleNames() {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Component.class));
        return scanner.findCandidateComponents("com.itsm.batch.job").stream()
                .map(bd -> bd.getBeanClassName().substring(bd.getBeanClassName().lastIndexOf('.') + 1))
                .filter(n -> n.endsWith("Job"))
                .collect(Collectors.toCollection(TreeSet::new));
    }

    @Test
    @DisplayName("sql/01_ddl.sql 에 tb_batch_job 테이블 정의가 있다")
    void ddlDefinesBatchJobTable() throws Exception {
        String ddl = Files.readString(repoSql("01_ddl.sql"), StandardCharsets.UTF_8);
        assertThat(ddl).contains("CREATE TABLE IF NOT EXISTS tb_batch_job");
        for (String col : List.of("job_name", "cron_expression", "is_active", "trigger_now",
                "last_executed_at", "last_result", "last_result_message")) {
            assertThat(ddl).as("tb_batch_job.%s", col).contains(col);
        }
    }

    @Test
    @DisplayName("sql/02_dml.sql 의 tb_batch_job 시드 job_name 이 실제 잡 클래스(@Component *Job) 와 1:1 로 일치한다")
    void seedJobNamesMatchJobClasses() throws Exception {
        String dml = Files.readString(repoSql("02_dml.sql"), StandardCharsets.UTF_8);
        Set<String> seeded = seededJobNames(dml);
        Set<String> classes = jobClassSimpleNames();

        assertThat(classes).as("잡 클래스 스캔").hasSizeGreaterThanOrEqualTo(15);
        assertThat(seeded).as("시드에만 있고 클래스가 없는 잡(스케줄러 등록 실패)").isSubsetOf(classes);
        assertThat(classes).as("클래스는 있는데 시드가 없는 잡(영원히 안 도는 배치)").isSubsetOf(seeded);
    }

    @Test
    @DisplayName("운영 반영용 phase28 스크립트는 멱등(INSERT IGNORE + CREATE IF NOT EXISTS)이고 시드와 같은 잡을 담는다")
    void prodMigrationIsIdempotentAndConsistent() throws Exception {
        String dml = Files.readString(repoSql("02_dml.sql"), StandardCharsets.UTF_8);
        String prod = Files.readString(repoSql("phase28_p4_batch_job_ddl_seed.sql"), StandardCharsets.UTF_8);
        assertThat(prod).contains("CREATE TABLE IF NOT EXISTS tb_batch_job");
        assertThat(prod).contains("INSERT IGNORE INTO tb_batch_job");
        assertThat(seededJobNames(prod.replace("INSERT IGNORE INTO", "INSERT INTO")))
                .isEqualTo(seededJobNames(dml));
    }
}
