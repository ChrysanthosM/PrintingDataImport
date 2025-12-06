package org.masouras.app.batch.pmp.control.business;

import lombok.extern.slf4j.Slf4j;
import org.masouras.squad.printing.mssql.schema.jpa.entity.PrintingDataEntity;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.validator.ValidationException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PmpStep1Processor1 implements ItemProcessor<PrintingDataEntity, PrintingDataEntity> {
    @Override
    public PrintingDataEntity process(PrintingDataEntity record) {
        log.info("Step1: Validating record {}", record.getId());
        if (record.getId() == null) {
            throw new ValidationException("Record ID cannot be null");
        }
        return record;
    }
}

