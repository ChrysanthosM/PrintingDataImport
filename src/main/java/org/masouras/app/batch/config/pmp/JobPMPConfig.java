package org.masouras.app.batch.config.pmp;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JobPMPConfig {
    public static final String JOB_NAME = "PMP_JOB";

    private final JobRepository jobRepository;

    @Autowired
    public JobPMPConfig(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @Bean
    public Job pmpJob(
            @Qualifier("pmpStep1") Step pmpStep1,
            @Qualifier("pmpStep2") Step pmpStep2,
            @Qualifier("pmpStep3") Step pmpStep3) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .start(pmpStep1)
                .next(pmpStep2)
                .next(pmpStep3)
                .build();
    }
}
