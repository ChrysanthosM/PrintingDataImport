package org.masouras.app.batch.pmp.control.business;

import lombok.extern.slf4j.Slf4j;
import org.masouras.squad.printing.mssql.schema.jpa.entity.PrintingDataEntity;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PmpStep1Processor2 implements ItemProcessor<PrintingDataEntity, PrintingDataEntity> {
    @Override
    public PrintingDataEntity process(PrintingDataEntity record) {
        log.info("Step2: Step2 for record {}", record.getId());
        return record;
    }
}

