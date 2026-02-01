package org.masouras.app.batch.pmp.control.step.cycle.listener;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.masouras.model.mssql.schema.jpa.boundary.BatchStatisticsDetailsService;
import org.masouras.model.mssql.schema.jpa.control.entity.BatchStatisticsDetailsEntity;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@RequiredArgsConstructor
@Component
public class PmpStepExecutionListener implements StepExecutionListener {
    private final BatchStatisticsDetailsService batchStatisticsDetailsService;

    @Override
    public ExitStatus afterStep(@NonNull StepExecution stepExecution) {
        Duration duration = (stepExecution.getStartTime() != null && stepExecution.getEndTime() != null)
                ? Duration.between(stepExecution.getStartTime(), stepExecution.getEndTime())
                : null;

        BatchStatisticsDetailsEntity batchStatisticsDetailsEntity = batchStatisticsDetailsService.save(
                BatchStatisticsDetailsEntity.builder()
                        .jobExecutionID(stepExecution.getJobExecutionId())
                        .jobName(stepExecution.getJobExecution().getJobInstance().getJobName())
                        .stepName(stepExecution.getStepName())
                        .exitStatus(stepExecution.getExitStatus().getExitCode())
                        .startTime(stepExecution.getStartTime())
                        .endTime(stepExecution.getEndTime())
                        .durationMS(duration == null ? 0L : duration.toMillis())
                        .readCount(stepExecution.getReadCount())
                        .writeCount(stepExecution.getWriteCount())
                        .commitCount(stepExecution.getCommitCount())
                        .rollbackCount(stepExecution.getRollbackCount())
                        .skipCount(stepExecution.getSkipCount())
                        .readSkipCount(stepExecution.getReadSkipCount())
                        .writeSkipCount(stepExecution.getWriteSkipCount())
                        .filterCount(stepExecution.getFilterCount())
                        .build());

        if (log.isInfoEnabled()) {
            log.info("=== {} STEP STATISTICS ===", stepExecution.getStepName());
            if (duration != null) {
                log.info("Duration: {}", String.format("%02d:%02d:%02d.%06d", duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart(), duration.toMillisPart()));
            }
            log.info("\n{}", ReflectionToStringBuilder.toString(batchStatisticsDetailsEntity, ToStringStyle.MULTI_LINE_STYLE));
        }

        if (stepExecution.getWriteCount() == 0) {
            if (log.isInfoEnabled()) log.info("ExitStatus changed to {}", ExitStatus.NOOP.getExitCode());
            return ExitStatus.NOOP;
        }
        return stepExecution.getExitStatus();
    }
}
