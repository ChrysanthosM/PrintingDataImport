package org.masouras.app.batch.pmp.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.InvalidJobParametersException;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.launch.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.JobRestartException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Set;

@Configuration
@EnableScheduling
@Slf4j
public class PmpSchedulerConfig {
    private final JobRepository jobRepository;
    private final JobOperator jobOperator;
    private final Job job;

    @Autowired
    public PmpSchedulerConfig(JobRepository jobRepository, JobOperator jobOperator, @Qualifier("pmpJob") Job job) {
        this.jobRepository = jobRepository;
        this.jobOperator = jobOperator;
        this.job = job;
    }

    @Scheduled(initialDelay = 10000, fixedRate = 10000)
    public void runJob() {
        Set<JobExecution> runningJobs = jobRepository.findRunningJobExecutions(PmpJobConfig.JOB_NAME);
        if (CollectionUtils.isNotEmpty(runningJobs)) {
            if (log.isInfoEnabled()) log.info("Job is still running, skipping this cycle...");
            return;
        }

        try {
            JobParameters params = new JobParametersBuilder()
                    .addLong("run.id", System.currentTimeMillis())
                    .toJobParameters();

            JobExecution execution = jobOperator.start(job, params);
            if (log.isInfoEnabled()) log.info("Job started with execution id: {} and status: {}", execution.getId(), execution.getStatus());
        } catch (JobInstanceAlreadyCompleteException | JobExecutionAlreadyRunningException | InvalidJobParametersException | JobRestartException e) {
            log.error("runJob failed with message: {}", e.getMessage(), e);
        }
    }

}
