package org.masouras.app.batch.pmp.control.business;

import lombok.extern.slf4j.Slf4j;
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


}
