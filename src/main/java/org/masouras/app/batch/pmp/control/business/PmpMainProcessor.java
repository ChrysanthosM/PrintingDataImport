package org.masouras.app.batch.pmp.control.business;

import lombok.extern.slf4j.Slf4j;
import org.masouras.squad.printing.mssql.schema.jpa.entity.PrintingDataEntity;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Slf4j
@Component
public class PmpMainProcessor {
    private final PmpMainProcessorValidation pmpMainProcessorValidation;
    private final PmpMainProcessorParser pmpMainProcessorParser;
    private final PmpMainProcessorFinalize pmpMainProcessorFinalize;

    @Autowired
    public PmpMainProcessor(PmpMainProcessorValidation pmpMainProcessorValidation, PmpMainProcessorParser pmpMainProcessorParser, PmpMainProcessorFinalize pmpMainProcessorFinalize) {
        this.pmpMainProcessorValidation = pmpMainProcessorValidation;
        this.pmpMainProcessorParser = pmpMainProcessorParser;
        this.pmpMainProcessorFinalize = pmpMainProcessorFinalize;
    }

    @Bean("pmpMainCompositeItemProcessor")
    public CompositeItemProcessor<PrintingDataEntity, PrintingDataEntity> pmpMainCompositeItemProcessor() {
        CompositeItemProcessor<PrintingDataEntity, PrintingDataEntity> compositeProcessor = new CompositeItemProcessor<>();
        compositeProcessor.setDelegates(Arrays.asList(pmpMainProcessorValidation, pmpMainProcessorParser, pmpMainProcessorFinalize));
        return compositeProcessor;
    }

}
