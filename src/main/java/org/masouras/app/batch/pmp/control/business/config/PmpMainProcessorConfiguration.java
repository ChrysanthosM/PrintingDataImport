package org.masouras.app.batch.pmp.control.business.config;

import org.masouras.app.batch.pmp.control.business.control.PmpMainProcessorFinalize;
import org.masouras.app.batch.pmp.control.business.control.PmpMainProcessorParser;
import org.masouras.app.batch.pmp.control.business.control.PmpMainProcessorValidation;
import org.masouras.squad.printing.mssql.schema.jpa.entity.PrintingDataEntity;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class PmpMainProcessorConfiguration {

    @Bean("pmpMainCompositeItemProcessor")
    public CompositeItemProcessor<PrintingDataEntity, PrintingDataEntity> pmpMainCompositeItemProcessor(
            PmpMainProcessorValidation pmpMainProcessorValidation,
            PmpMainProcessorParser pmpMainProcessorParser,
            PmpMainProcessorFinalize pmpMainProcessorFinalize) {

        CompositeItemProcessor<PrintingDataEntity, PrintingDataEntity> compositeProcessor = new CompositeItemProcessor<>();
        compositeProcessor.setDelegates(Arrays.asList(pmpMainProcessorValidation, pmpMainProcessorParser, pmpMainProcessorFinalize));
        return compositeProcessor;
    }
}
