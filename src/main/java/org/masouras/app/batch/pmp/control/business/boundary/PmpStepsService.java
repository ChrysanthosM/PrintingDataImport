package org.masouras.app.batch.pmp.control.business.boundary;

import lombok.extern.slf4j.Slf4j;
import org.masouras.app.batch.pmp.control.business.control.step.PmpStepNotificationService;
import org.masouras.app.batch.pmp.control.business.control.step.PmpStepReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PmpStepsService {
    private final PmpStepReportService pmpStepReportService;
    private final PmpStepNotificationService pmpStepNotificationService;

    @Autowired
    public PmpStepsService(PmpStepReportService pmpStepReportService, PmpStepNotificationService pmpStepNotificationService) {
        this.pmpStepReportService = pmpStepReportService;
        this.pmpStepNotificationService = pmpStepNotificationService;
    }

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
