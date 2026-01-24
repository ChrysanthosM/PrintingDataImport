package org.masouras.app.batch.pmp.control.step.business.processor;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.masouras.model.mssql.schema.jpa.control.entity.PrintingDataEntity;
import org.masouras.model.mssql.schema.jpa.control.entity.enums.PrintingStatus;
import org.springframework.batch.infrastructure.item.ItemProcessor;
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

