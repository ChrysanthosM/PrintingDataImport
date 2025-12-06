package org.masouras.app.batch.pmp.control.business;

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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Service
public class PmpStep1Service {
    private static final int CHUNK_SIZE = 10;

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final ItemReader<PrintingDataEntity> pmpReader;
    private final ItemProcessor<PrintingDataEntity, PrintingDataEntity> pmpProcessor;
    private final ItemWriter<PrintingDataEntity> pmpWriter;

    public PmpStep1Service(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                           ItemReader<PrintingDataEntity> pmpReader,
                           @Qualifier("pmpProcessor") ItemProcessor<PrintingDataEntity, PrintingDataEntity> pmpProcessor,
                           ItemWriter<PrintingDataEntity> pmpWriter) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
        this.pmpReader = pmpReader;
        this.pmpProcessor = pmpProcessor;
        this.pmpWriter = pmpWriter;
    }

    @Bean
    public Step pmpStep1() {
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
}
