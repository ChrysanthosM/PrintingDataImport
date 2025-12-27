package org.masouras.app.batch.pmp.boundary;

import org.masouras.app.batch.pmp.boundary.step.PmpMainProcessorFinalize;
import org.masouras.app.batch.pmp.boundary.step.PmpMainProcessorParser;
import org.masouras.app.batch.pmp.boundary.step.PmpMainProcessorValidation;
import org.masouras.model.mssql.schema.jpa.control.entity.PrintingDataEntity;
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
