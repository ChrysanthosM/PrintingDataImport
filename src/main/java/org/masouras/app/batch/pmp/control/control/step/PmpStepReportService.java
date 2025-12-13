package org.masouras.app.batch.pmp.control.control.step;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PmpStepReportService implements StepProcessor {

    @Override
    public boolean process() {
        return true;
    }
}
