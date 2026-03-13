package com.itsm.batch.config;

import com.itsm.core.domain.batch.BatchJob;
import com.itsm.core.repository.batch.BatchJobRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Component
@Slf4j
public class DynamicScheduler {

    private final BatchJobRepository batchJobRepository;
    private final ApplicationContext applicationContext;
    private final TaskScheduler taskScheduler;

    private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();
    private final Map<String, String> registeredCrons = new ConcurrentHashMap<>();

    public DynamicScheduler(BatchJobRepository batchJobRepository,
                            ApplicationContext applicationContext,
                            TaskScheduler taskScheduler) {
        this.batchJobRepository = batchJobRepository;
        this.applicationContext = applicationContext;
        this.taskScheduler = taskScheduler;
    }

    @PostConstruct
    public void init() {
        log.info("[DynamicScheduler] 초기 배치 작업 로딩");
        refreshSchedules();
    }

    @Scheduled(fixedDelay = 60000)
    public void refreshSchedules() {
        List<BatchJob> allJobs = batchJobRepository.findAll();

        for (BatchJob job : allJobs) {
            String jobName = job.getJobName();

            if (job.isEnabled()) {
                String currentCron = registeredCrons.get(jobName);
                if (currentCron == null || !currentCron.equals(job.getCronExpression())) {
                    // Cancel existing if cron changed
                    cancelTask(jobName);
                    scheduleTask(job);
                }
            } else {
                // Job disabled - cancel if running
                if (scheduledTasks.containsKey(jobName)) {
                    cancelTask(jobName);
                    log.info("[DynamicScheduler] 비활성 배치 해제: {}", jobName);
                }
            }
        }

        // Cancel tasks for jobs that no longer exist in DB
        scheduledTasks.keySet().stream()
                .filter(name -> allJobs.stream().noneMatch(j -> j.getJobName().equals(name)))
                .toList()
                .forEach(this::cancelTask);
    }

    private void scheduleTask(BatchJob batchJob) {
        String jobName = batchJob.getJobName();
        try {
            Object bean = applicationContext.getBean(
                    Character.toLowerCase(jobName.charAt(0)) + jobName.substring(1));
            Method executeMethod = bean.getClass().getMethod("execute");

            Runnable task = () -> {
                try {
                    log.info("[DynamicScheduler] 배치 실행: {}", jobName);
                    executeMethod.invoke(bean);
                    recordResult(jobName, "SUCCESS", null);
                } catch (Exception e) {
                    log.error("[DynamicScheduler] 배치 실패: {}", jobName, e);
                    String msg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
                    recordResult(jobName, "FAILURE", msg);
                }
            };

            ScheduledFuture<?> future = taskScheduler.schedule(task,
                    new CronTrigger(batchJob.getCronExpression()));
            scheduledTasks.put(jobName, future);
            registeredCrons.put(jobName, batchJob.getCronExpression());
            log.info("[DynamicScheduler] 배치 등록: {} [{}]", jobName, batchJob.getCronExpression());
        } catch (Exception e) {
            log.error("[DynamicScheduler] 배치 등록 실패: {}", jobName, e);
        }
    }

    private void cancelTask(String jobName) {
        ScheduledFuture<?> future = scheduledTasks.remove(jobName);
        if (future != null) {
            future.cancel(false);
        }
        registeredCrons.remove(jobName);
    }

    private void recordResult(String jobName, String result, String message) {
        try {
            batchJobRepository.findByJobName(jobName).ifPresent(job -> {
                job.recordExecution(result, message);
                batchJobRepository.save(job);
            });
        } catch (Exception e) {
            log.error("[DynamicScheduler] 실행 결과 기록 실패: {}", jobName, e);
        }
    }
}
