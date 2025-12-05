package org.masouras.app.batch.config.pmp;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
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

@Configuration
@Slf4j
public class StepsConfig {
    private static final int CHUNK_SIZE = 10;

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Autowired
    public StepsConfig(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
    }

    @Bean
    public Step pmpStep1(ItemReader<PrintingDataEntity> pmpReader,
                         @Qualifier("pmpProcessor") ItemProcessor<PrintingDataEntity, PrintingDataEntity> pmpProcessor,
                         ItemWriter<PrintingDataEntity> pmpWriter) {
        return new StepBuilder("pmpStep1", jobRepository)
                .<PrintingDataEntity, PrintingDataEntity>chunk(CHUNK_SIZE, transactionManager)
                .reader(pmpReader)
                .processor(pmpProcessor)
                .writer(pmpWriter)
                .listener(new StepExecutionListener() {
                    @Override
                    public ExitStatus afterStep(@NotNull StepExecution stepExecution) {
                        if (stepExecution.getWriteCount() == 0) {
                            return new ExitStatus("NOOP");
                        }
                        return stepExecution.getExitStatus();
                    }
                })
                .build();
    }

    @Bean
    public Step pmpStep2() {
        return new StepBuilder("pmpStep2", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    if (log.isInfoEnabled()) log.info("Generating summary report...");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step pmpStep3() {
        return new StepBuilder("pmpStep3", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    if (log.isInfoEnabled()) log.info("Sending notification...");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}
