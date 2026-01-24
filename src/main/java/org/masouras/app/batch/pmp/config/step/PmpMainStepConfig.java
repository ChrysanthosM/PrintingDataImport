package org.masouras.app.batch.pmp.config.step;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.masouras.app.batch.pmp.control.step.business.processor.PmpMainStepCompositeItemProcessor;
import org.masouras.app.batch.pmp.control.step.cycle.PmpMainStepSkipPolicy;
import org.masouras.app.batch.pmp.control.step.cycle.listener.PmpItemProcessListener;
import org.masouras.app.batch.pmp.control.step.cycle.listener.PmpProcessListener;
import org.masouras.app.batch.pmp.control.step.cycle.listener.PmpSkipListener;
import org.masouras.app.batch.pmp.control.step.cycle.listener.PmpStepExecutionListener;
import org.masouras.model.mssql.schema.jpa.control.entity.PrintingDataEntity;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class PmpMainStepConfig {
    private static final int CHUNK_SIZE = 10;

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final ItemReader<PrintingDataEntity> pmpReader;
    private final PmpMainStepCompositeItemProcessor pmpProcessor;
    private final ItemWriter<PrintingDataEntity> pmpWriter;
    private final PmpMainStepSkipPolicy pmpMainStepSkipPolicy;
    private final PmpProcessListener pmpProcessListener;
    private final PmpSkipListener pmpSkipListener;
    private final PmpItemProcessListener pmpItemProcessListener;
    private final PmpStepExecutionListener pmpStepExecutionListener;

    @Bean
    public Step pmpMainStep() {
        return new StepBuilder("pmpMainStep", jobRepository)
                .<PrintingDataEntity, PrintingDataEntity>chunk(CHUNK_SIZE)
                .transactionManager(transactionManager)
                .reader(pmpReader)
                .processor(pmpProcessor)
                .writer(pmpWriter)
                .faultTolerant()
                .skipPolicy(pmpMainStepSkipPolicy)
                .skipLimit(Integer.MAX_VALUE)
                .skipListener(pmpSkipListener)
                .allowStartIfComplete(true)
                .listener(pmpProcessListener)
                .listener(pmpItemProcessListener)
                .listener(pmpStepExecutionListener)
                .build();
    }
}
