package org.masouras.app.batch.pmp.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PmpJobConfig {
    public static final String JOB_NAME = "PMP_JOB";

    private final JobRepository jobRepository;

    @Autowired
    public PmpJobConfig(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @Bean
    public Job pmpJob(@Qualifier("pmpMainStep") Step pmpMainStep,
                      @Qualifier("pmpReportStep") Step pmpReportStep,
                      @Qualifier("pmpNotifyStep") Step pmpNotifyStep) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .start(pmpMainStep).on("NOOP").end()
                .from(pmpMainStep).on("*").to(pmpReportStep)
                .next(pmpNotifyStep)
                .end()
                .build();
    }
}
