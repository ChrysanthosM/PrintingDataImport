package org.masouras.app.batch.pmp.control.business;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.masouras.squad.printing.mssql.schema.jpa.entity.PrintingDataEntity;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PmpMainProcessorFinalize implements ItemProcessor<PrintingDataEntity, PrintingDataEntity> {
    @Override
    public PrintingDataEntity process(@NotNull PrintingDataEntity printingDataEntity) {
        if (log.isInfoEnabled()) log.info("{}: Finalizing printingDataEntity {}", this.getClass().getSimpleName(), printingDataEntity.getId());
        printingDataEntity.setProcessed(true);
        return printingDataEntity;
    }
}

