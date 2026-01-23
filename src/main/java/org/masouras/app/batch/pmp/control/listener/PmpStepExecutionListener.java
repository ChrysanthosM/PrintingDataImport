package org.masouras.app.batch.pmp.control.listener;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PmpStepExecutionListener implements StepExecutionListener {

    @Override
    public ExitStatus afterStep(@NonNull StepExecution stepExecution) {

        if (log.isInfoEnabled()) {
            log.info("=== STEP STATISTICS ===");
            log.info("Read count: {}", stepExecution.getReadCount());
            log.info("Write count: {}", stepExecution.getWriteCount());
            log.info("Skip count: {}", stepExecution.getSkipCount());
            log.info("Process skip count: {}", stepExecution.getProcessSkipCount());
        }

        if (stepExecution.getWriteCount() == 0) {
            log.info("pmpMainStep ExitStatus NOOP");
            return new ExitStatus("NOOP");
        }

        return stepExecution.getExitStatus();
    }
}
