package org.masouras.app.batch.pmp.config.step;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.masouras.app.batch.pmp.control.step.business.processor.PmpReportProcessor;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class PmpReportStepConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final PmpReportProcessor pmpReportProcessor;

    @Bean
    public Step pmpReportStep() {
        return new StepBuilder("pmpReportStep", jobRepository)
                .tasklet((_, _) -> {
                    pmpReportProcessor.process();
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}
