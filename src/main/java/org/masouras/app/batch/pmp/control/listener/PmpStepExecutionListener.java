package org.masouras.app.batch.pmp.control.listener;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class PmpStepExecutionListener implements StepExecutionListener {

    @Override
    public ExitStatus afterStep(@NonNull StepExecution stepExecution) {

        if (log.isInfoEnabled()) {
            log.info("=== STEP STATISTICS ===");
            if (stepExecution.getStartTime() != null) log.info("Start Time: {}", stepExecution.getStartTime().toInstant(ZoneOffset.of("+02:00")).atZone(ZoneId.of("Europe/Athens")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSSS")));
            if (stepExecution.getEndTime() != null) log.info("End Time: {}", stepExecution.getEndTime().toInstant(ZoneOffset.of("+02:00")).atZone(ZoneId.of("Europe/Athens")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSSS")));
            if (stepExecution.getStartTime() != null && stepExecution.getEndTime() != null) {
                Duration duration = Duration.between(stepExecution.getStartTime(), stepExecution.getEndTime());
                log.info("Duration: {}", String.format("%02d:%02d:%02d", duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart()));
            }

            log.info("Read count: {}", stepExecution.getReadCount());
            log.info("Write count: {}", stepExecution.getWriteCount());
            log.info("Commit count: {}", stepExecution.getCommitCount());
            log.info("Rollback count: {}", stepExecution.getRollbackCount());
            log.info("Skip count: {}", stepExecution.getSkipCount());
            log.info("Read Skip count: {}", stepExecution.getReadSkipCount());
            log.info("Process Skip count: {}", stepExecution.getProcessSkipCount());
            log.info("Write skip count: {}", stepExecution.getWriteSkipCount());
            log.info("Filter count: {}", stepExecution.getFilterCount());
        }

        if (stepExecution.getWriteCount() == 0) {
            log.info("pmpMainStep ExitStatus NOOP");
            return new ExitStatus("NOOP");
        }

        return stepExecution.getExitStatus();
    }
}
