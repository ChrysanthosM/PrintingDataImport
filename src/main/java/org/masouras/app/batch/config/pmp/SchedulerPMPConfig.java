package org.masouras.app.batch.config.pmp;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Set;

@Configuration
@EnableScheduling
@Slf4j
public class SchedulerPMPConfig {
    private final JobLauncher jobLauncher;
    private final JobExplorer jobExplorer;
    private final Job job;

    @Autowired
    public SchedulerPMPConfig(JobLauncher jobLauncher, JobExplorer jobExplorer,
                              @Qualifier("pmpJob") Job job) {
        this.jobLauncher = jobLauncher;
        this.jobExplorer = jobExplorer;
        this.job = job;
    }

    @Scheduled(fixedRate = 10000)
    public void runJob() throws Exception {
        Set<JobExecution> runningJobs = jobExplorer.findRunningJobExecutions(JobPMPConfig.JOB_NAME);
        if (CollectionUtils.isNotEmpty(runningJobs)) {
            if (log.isInfoEnabled()) log.info("Job is still running, skipping this cycle...");
            return;
        }

        JobParameters params = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();
        jobLauncher.run(job, params);
    }
}
