package org.masouras.app.batch.pmp.boundary.step;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.masouras.squad.printing.mssql.schema.jpa.control.PrintingStatus;
import org.masouras.squad.printing.mssql.schema.jpa.entity.PrintingDataEntity;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PmpMainProcessorFinalize implements ItemProcessor<PrintingDataEntity, PrintingDataEntity> {
    @Override
    public PrintingDataEntity process(@NotNull PrintingDataEntity printingDataEntity) {
        if (log.isInfoEnabled()) log.info("{}: Finalizing printingDataEntity {}", this.getClass().getSimpleName(), printingDataEntity.getId());
        printingDataEntity.setPrintingStatus(PrintingStatus.PROCESSED);
        return printingDataEntity;
    }
}

