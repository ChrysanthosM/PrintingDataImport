package org.masouras.app.batch.pmp.control.listener;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.masouras.data.boundary.RepositoryFacade;
import org.masouras.model.mssql.schema.jpa.control.entity.PrintingDataEntity;
import org.springframework.batch.core.listener.SkipListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class PmpSkipListener implements SkipListener<PrintingDataEntity, PrintingDataEntity> {
    private final RepositoryFacade repositoryFacade;

    @Override
    public void onSkipInRead(Throwable throwable) {
        log.warn("▶▶▶ Skipped in READ: {}", throwable.getMessage());
    }

    @Override
    public void onSkipInProcess(PrintingDataEntity item, @NonNull Throwable throwable) {
        log.warn("▶▶▶ Skipped in PROCESS - item id: {}, exception: {}",
                item != null ? item.getId() : "null",
                throwable.getMessage());

        if (item != null) {
            try {
                repositoryFacade.saveStepFailed(item, throwable.getMessage());
                log.info("Successfully saved failed item: {}", item.getId());
            } catch (Exception e) {
                log.error("Failed to save error record for item: {}", item.getId(), e);
            }
        }
    }

    @Override
    public void onSkipInWrite(PrintingDataEntity item, Throwable throwable) {
        log.warn("▶▶▶ Skipped in WRITE - item id: {}, exception: {}",
                item != null ? item.getId() : "null",
                throwable.getMessage());
    }
}
