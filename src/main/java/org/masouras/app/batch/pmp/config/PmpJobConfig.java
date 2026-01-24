package org.masouras.app.batch.pmp.config;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class PmpJobConfig {
    public static final String JOB_NAME = "PMP_JOB";

    private final JobRepository jobRepository;

    @Bean
    public Job pmpJob(@Qualifier("pmpMainStep") Step pmpMainStep,
                      @Qualifier("pmpReportStep") Step pmpReportStep) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .start(pmpMainStep).on(ExitStatus.NOOP.getExitCode()).end()
                .from(pmpMainStep).on("*").to(pmpReportStep)
                .end()
                .build();
    }
}
