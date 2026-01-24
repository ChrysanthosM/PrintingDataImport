package org.masouras.app.batch.pmp.control.step.business.processor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PmpReportProcessor implements StepProcessor {

    @Override
    public boolean process() {
        if (log.isInfoEnabled()) log.info("Generating summary report...");
        boolean processed = true;
        if (log.isInfoEnabled()) log.info("Summary report finished OK {}", processed);

        return processed;
    }
}
