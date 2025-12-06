package org.masouras.app.batch.pmp.control.business;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PmpStepsService {
    private final PmpMainStepService pmpMainStepService;
    private final PmpReportStepService pmpReportStepService;
    private final PmpNotifyStepService pmpNotifyStepService;

    @Autowired
    public PmpStepsService(PmpMainStepService pmpMainStepService, PmpReportStepService pmpReportStepService, PmpNotifyStepService pmpNotifyStepService) {
        this.pmpMainStepService = pmpMainStepService;
        this.pmpReportStepService = pmpReportStepService;
        this.pmpNotifyStepService = pmpNotifyStepService;
    }


}
