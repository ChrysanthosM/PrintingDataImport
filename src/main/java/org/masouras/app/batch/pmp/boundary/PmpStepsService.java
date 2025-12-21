package org.masouras.app.batch.pmp.boundary;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.masouras.app.batch.pmp.boundary.step.PmpStepNotificationService;
import org.masouras.app.batch.pmp.boundary.step.PmpStepReportService;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PmpStepsService {
    private final PmpStepReportService pmpStepReportService;
    private final PmpStepNotificationService pmpStepNotificationService;

    public void processReport() {
        if (log.isInfoEnabled()) log.info("Generating summary report...");
        boolean processed = pmpStepReportService.process();
        if (log.isInfoEnabled()) log.info("Summary report finished OK {}", processed);
    }

    public void processNotification() {
        if (log.isInfoEnabled()) log.info("Sending notification...");
        boolean processed = pmpStepNotificationService.process();
        if (log.isInfoEnabled()) log.info("Notification finished OK {}", processed);
    }
}
