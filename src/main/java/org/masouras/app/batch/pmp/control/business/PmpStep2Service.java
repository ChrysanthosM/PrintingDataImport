package org.masouras.app.batch.pmp.control.business;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Service
public class PmpStep2Service {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    public PmpStep2Service(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
    }

    @Bean
    public Step pmpStep2() {
        return new StepBuilder("pmpStep2", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    log.info("Generating summary report...");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}
