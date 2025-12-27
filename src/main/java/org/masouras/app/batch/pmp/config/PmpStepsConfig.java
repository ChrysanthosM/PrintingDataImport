package org.masouras.app.batch.pmp.config;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.masouras.app.batch.pmp.boundary.PmpStepsService;
import org.masouras.data.boundary.RepositoryFacade;
import org.masouras.model.mssql.schema.jpa.control.entity.PrintingDataEntity;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.listener.SkipListener;
import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.validator.ValidationException;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
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
    private final RepositoryFacade repositoryFacade;

    public PmpStepsConfig(JobRepository jobRepository,
                          PlatformTransactionManager transactionManager,
                          ItemReader<PrintingDataEntity> pmpReader,
                          @Qualifier("pmpMainCompositeItemProcessor")
                          ItemProcessor<PrintingDataEntity, PrintingDataEntity> pmpProcessor,
                          ItemWriter<PrintingDataEntity> pmpWriter,
                          PmpStepsService pmpStepsService,
                          RepositoryFacade repositoryFacade) {

        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
        this.pmpReader = pmpReader;
        this.pmpProcessor = pmpProcessor;
        this.pmpWriter = pmpWriter;
        this.pmpStepsService = pmpStepsService;
        this.repositoryFacade = repositoryFacade;
    }

    @Bean
    public SkipPolicy pmpMainStepSkipPolicy() {
        return (throwable, skipCount) -> throwable instanceof ValidationException;
    }

    @Bean("pmpMainStep")
    public Step pmpMainStep() {
        return new StepBuilder("pmpMainStep", jobRepository)
                .<PrintingDataEntity, PrintingDataEntity>chunk(CHUNK_SIZE)
                .transactionManager(transactionManager)

                .reader(pmpReader)
                .processor(pmpProcessor)
                .writer(pmpWriter)

                .faultTolerant()
                .skipPolicy(pmpMainStepSkipPolicy())
                .skipLimit(Integer.MAX_VALUE)
                .listener(new SkipListener<PrintingDataEntity, PrintingDataEntity>() {
                    @Override
                    public void onSkipInProcess(PrintingDataEntity item, @NonNull Throwable throwable) {
                        log.warn("Skipped item id {} due to {}", item.getId(), throwable.getMessage());
                        repositoryFacade.saveStepFailed(item, throwable.getMessage());
                    }
                })
                .allowStartIfComplete(true)

                .listener(new StepExecutionListener() {
                    @Override
                    public ExitStatus afterStep(@NonNull StepExecution stepExecution) {
                        if (stepExecution.getWriteCount() == 0) {
                            log.info("pmpMainStep ExitStatus NOOP");
                            return new ExitStatus("NOOP");
                        }
                        return stepExecution.getExitStatus();
                    }
                })
                .build();
    }

    // -------------------------------
    // NOTIFY STEP (Tasklet)
    // -------------------------------
    @Bean("pmpNotifyStep")
    public Step pmpNotifyStep() {
        return new StepBuilder("pmpNotifyStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    pmpStepsService.processNotification();
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    // -------------------------------
    // REPORT STEP (Tasklet)
    // -------------------------------
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