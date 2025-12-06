package org.masouras.app.batch.pmp.control.business;

import lombok.extern.slf4j.Slf4j;
import org.masouras.squad.printing.mssql.schema.jpa.entity.PrintingDataEntity;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PmpStep1Processor3 implements ItemProcessor<PrintingDataEntity, PrintingDataEntity> {
    @Override
    public PrintingDataEntity process(PrintingDataEntity record) {
        log.info("Step3: Step3 for record {}", record.getId());
        record.setProcessed(true);
        return record;
    }
}

