package org.masouras.app.batch.pmp.control.step.cycle.listener;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.masouras.util.DateTimeUtils;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
public class PmpStepExecutionListener implements StepExecutionListener {

    @Override
    public ExitStatus afterStep(@NonNull StepExecution stepExecution) {

        if (log.isInfoEnabled()) {
            log.info("=== {} STEP STATISTICS ===", stepExecution.getStepName());
            log.info("ExitStatus: {}", stepExecution.getExitStatus());
            if (stepExecution.getStartTime() != null) log.info("Start Time: {}", DateTimeUtils.formatToAthens(stepExecution.getStartTime()));
            if (stepExecution.getEndTime() != null) log.info("End Time: {}", DateTimeUtils.formatToAthens(stepExecution.getEndTime()));
            if (stepExecution.getStartTime() != null && stepExecution.getEndTime() != null) {
                Duration duration = Duration.between(stepExecution.getStartTime(), stepExecution.getEndTime());
                String formatted = String.format("%02d:%02d:%02d.%04d", duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart(), duration.toMillisPart());
                log.info("Duration: {}", formatted);
            }

            log.info("Read Count: {}", stepExecution.getReadCount());
            log.info("Write Count: {}", stepExecution.getWriteCount());
            log.info("Commit Count: {}", stepExecution.getCommitCount());
            log.info("Rollback Count: {}", stepExecution.getRollbackCount());
            log.info("Skip Count: {}", stepExecution.getSkipCount());
            log.info("Read Skip Count: {}", stepExecution.getReadSkipCount());
            log.info("Process Skip Count: {}", stepExecution.getProcessSkipCount());
            log.info("Write Skip Count: {}", stepExecution.getWriteSkipCount());
            log.info("Filter Count: {}", stepExecution.getFilterCount());
        }

        if (stepExecution.getWriteCount() == 0) {
            if (log.isInfoEnabled()) log.info("ExitStatus changed to NOOP");
            return new ExitStatus("NOOP");
        }
        return stepExecution.getExitStatus();
    }
}
