package org.masouras.app.batch.pmp.config;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.masouras.app.batch.pmp.control.business.boundary.PmpStepsService;
import org.masouras.squad.printing.mssql.schema.jpa.entity.PrintingDataEntity;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
public class PmpStepsConfig {
    private static final int CHUNK_SIZE = 10;

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final ItemReader<PrintingDataEntity> pmpReader;
    private final ItemProcessor<PrintingDataEntity, PrintingDataEntity> pmpProcessor;
    private final ItemWriter<PrintingDataEntity> pmpWriter;

    private final PmpStepsService pmpStepsService;

    @Autowired
    public PmpStepsConfig(JobRepository jobRepository,
                          PlatformTransactionManager transactionManager,
                          ItemReader<PrintingDataEntity> pmpReader,
                          @Qualifier("pmpMainCompositeItemProcessor") ItemProcessor<PrintingDataEntity, PrintingDataEntity> pmpProcessor,
                          ItemWriter<PrintingDataEntity> pmpWriter,
                          PmpStepsService pmpStepsService) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
        this.pmpReader = pmpReader;
        this.pmpProcessor = pmpProcessor;
        this.pmpWriter = pmpWriter;
        this.pmpStepsService = pmpStepsService;
    }

    @Bean("pmpMainStep")
    public Step pmpMainStep() {
        return new StepBuilder("pmpMainStep", jobRepository)
                .<PrintingDataEntity, PrintingDataEntity>chunk(CHUNK_SIZE, transactionManager)
                .reader(pmpReader)
                .processor(pmpProcessor)
                .writer(pmpWriter)
                .allowStartIfComplete(true)
                .listener(new StepExecutionListener() {
                    @Override
                    public ExitStatus afterStep(@NotNull StepExecution stepExecution) {
                        if (stepExecution.getWriteCount() == 0) {
                            if (log.isInfoEnabled()) log.info("pmpMainStep ExitStatus NOOP");
                            return new ExitStatus("NOOP");
                        }
                        return stepExecution.getExitStatus();
                    }
                })
                .build();
    }

    @Bean("pmpNotifyStep")
    public Step pmpNotifyStep() {
        return new StepBuilder("pmpNotifyStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    pmpStepsService.processNotification();
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean("pmpReportStep")
    public Step pmpReportStep() {
        return new StepBuilder("pmpReportStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    pmpStepsService.processReport();
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}
