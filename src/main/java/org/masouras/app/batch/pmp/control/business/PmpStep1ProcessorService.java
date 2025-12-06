package org.masouras.app.batch.pmp.control.business;

import lombok.extern.slf4j.Slf4j;
import org.masouras.squad.printing.mssql.schema.jpa.entity.PrintingDataEntity;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Slf4j
@Service
public class PmpStep1ProcessorService {
    private final PmpStep1Processor1 pmpStep1Processor1;
    private final PmpStep1Processor2 pmpStep1Processor2;
    private final PmpStep1Processor3 pmpStep1Processor3;

    @Autowired
    public PmpStep1ProcessorService(PmpStep1Processor1 pmpStep1Processor1, PmpStep1Processor2 pmpStep1Processor2, PmpStep1Processor3 pmpStep1Processor3) {
        this.pmpStep1Processor1 = pmpStep1Processor1;
        this.pmpStep1Processor2 = pmpStep1Processor2;
        this.pmpStep1Processor3 = pmpStep1Processor3;
    }

    @Bean
    public CompositeItemProcessor<PrintingDataEntity, PrintingDataEntity> pmpProcessor() {
        CompositeItemProcessor<PrintingDataEntity, PrintingDataEntity> compositeProcessor = new CompositeItemProcessor<>();
        compositeProcessor.setDelegates(Arrays.asList(pmpStep1Processor1, pmpStep1Processor2, pmpStep1Processor3));
        return compositeProcessor;
    }

}
