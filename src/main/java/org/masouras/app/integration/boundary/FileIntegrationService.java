package org.masouras.app.integration.boundary;

import jakarta.annotation.Nullable;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.Validate;
import org.masouras.app.artemis.business.printing.boundary.ProduceArtemisPrintingJob;
import org.masouras.app.artemis.business.printing.model.PrintingJobMessage;
import org.masouras.facade.FilesFacade;
import org.masouras.facade.PrintingDataEntityFacade;
import org.masouras.model.mssql.schema.jpa.control.entity.PrintingDataEntity;
import org.masouras.model.mssql.schema.jpa.control.entity.enums.PrintingWayType;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileIntegrationService {
    private final FilesFacade filesFacade;
    private final PrintingDataEntityFacade printingDataEntityFacade;
    private final ProduceArtemisPrintingJob produceArtemisPrintingJob;

    public Long handleAndInitialPersistPrintingData(@NonNull File triggerFile) {
        return printingDataEntityFacade.initialPersist(triggerFile);
    }

    public PrintingDataEntity handleAndValidatePrintingData(@NonNull Long insertedId, @Nullable PrintingWayType checkPrintingWayType) {
        return printingDataEntityFacade.validatePrintingDataEntity(insertedId, checkPrintingWayType);
    }

    public boolean handleAndSendPrintingDataToArtemis(@NonNull Long printingId) {
        produceArtemisPrintingJob.send(new PrintingJobMessage(printingId));
        return true;
    }


    public void handleErrorFile(@NonNull File triggerFile, String errorFolder) {
        Validate.notBlank(errorFolder);

        filesFacade.copyFile(triggerFile, errorFolder);
        List<String> possibleRelevantFileNames = filesFacade.getPossibleRelevantFileNames(triggerFile);
        if (CollectionUtils.isEmpty(possibleRelevantFileNames)) return;
        possibleRelevantFileNames.stream()
                .map(File::new)
                .filter(file -> file.exists() && file.isFile())
                .forEach(file -> filesFacade.moveFile(file, errorFolder));
    }


}
