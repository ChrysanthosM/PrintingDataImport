package org.masouras.app.batch.config.pmp;

import lombok.extern.slf4j.Slf4j;
import org.masouras.squad.printing.mssql.schema.jpa.entity.PrintingDataEntity;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.batch.item.validator.ValidationException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
@Slf4j
public class ProcessorConfig {

    @Bean
    public CompositeItemProcessor<PrintingDataEntity, PrintingDataEntity> pmpProcessor(
            @Qualifier("pmpProcessorStep1") ItemProcessor<PrintingDataEntity, PrintingDataEntity> pmpProcessorStep1,
            @Qualifier("pmpProcessorStep2") ItemProcessor<PrintingDataEntity, PrintingDataEntity> pmpProcessorStep2,
            @Qualifier("pmpProcessorStep3") ItemProcessor<PrintingDataEntity, PrintingDataEntity> pmpProcessorStep3) {
        CompositeItemProcessor<PrintingDataEntity, PrintingDataEntity> compositeProcessor = new CompositeItemProcessor<>();
        compositeProcessor.setDelegates(Arrays.asList(pmpProcessorStep1, pmpProcessorStep2, pmpProcessorStep3));
        return compositeProcessor;
    }

    @Bean
    public ItemProcessor<PrintingDataEntity, PrintingDataEntity> pmpProcessorStep1() {
        return record -> {
            if (log.isInfoEnabled()) log.info("Step1: Validating record {}", record.getId());
            if (record.getId() == null) {
                throw new ValidationException("Record ID cannot be null");
            }
            return record;
        };
    }

    @Bean
    public ItemProcessor<PrintingDataEntity, PrintingDataEntity> pmpProcessorStep2() {
        return record -> {
            if (log.isInfoEnabled()) log.info("Step2: Step2 for record {}", record.getId());
            return record;
        };
    }

    @Bean
    public ItemProcessor<PrintingDataEntity, PrintingDataEntity> pmpProcessorStep3() {
        return record -> {
            if (log.isInfoEnabled()) log.info("Step3: Marking record {} as processed", record.getId());
            record.setProcessed(true);
            return record;
        };
    }
}
