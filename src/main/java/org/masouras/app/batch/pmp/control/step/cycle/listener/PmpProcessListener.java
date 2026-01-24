package org.masouras.app.batch.pmp.control.step.cycle.listener;

import lombok.extern.slf4j.Slf4j;
import org.masouras.model.mssql.schema.jpa.control.entity.PrintingDataEntity;
import org.springframework.batch.core.listener.ItemProcessListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PmpProcessListener implements ItemProcessListener<PrintingDataEntity, PrintingDataEntity> {

    @Override
    public void beforeProcess(PrintingDataEntity item) {
        if (log.isDebugEnabled()) log.debug("Before processing item: {}", item.getId());
    }

    @Override
    public void afterProcess(PrintingDataEntity item, PrintingDataEntity result) {
        if (log.isDebugEnabled()) log.debug("After processing item: {} -> {}",
                item != null ? item.getId() : "null",
                result != null ? result.getId() : "null");
    }

    @Override
    public void onProcessError(PrintingDataEntity item, Exception e) {
        log.error("▶▶▶ PROCESS ERROR detected: item={}, exception={}",
                item != null ? item.getId() : "null",
                e.getClass().getName() + ": " + e.getMessage());
    }
}
