package org.masouras.app.batch.pmp.control.business.control;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PmpStepNotificationService implements StepProcessor {

    @Override
    public boolean process() {
        return true;
    }
}
