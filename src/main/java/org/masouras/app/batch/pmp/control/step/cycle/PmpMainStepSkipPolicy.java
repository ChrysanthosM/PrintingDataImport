package org.masouras.app.batch.pmp.control.step.cycle;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.batch.core.step.skip.SkipLimitExceededException;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.infrastructure.item.validator.ValidationException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PmpMainStepSkipPolicy implements SkipPolicy {

    @Override
    public boolean shouldSkip(@NonNull Throwable throwable, long skipCount) throws SkipLimitExceededException {
        Throwable cause = throwable;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }

        if (cause instanceof ValidationException) {
            if (log.isWarnEnabled()) {
                log.warn("Skipping ValidationException: {}", cause.getMessage());
            }
            return true;
        }

        return false;
    }
}
