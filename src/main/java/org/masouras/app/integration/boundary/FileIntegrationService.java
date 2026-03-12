package org.masouras.app.integration.boundary;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.Validate;
import org.masouras.facade.FilesFacade;
import org.masouras.facade.PrintingDataEntityFacade;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileIntegrationService {
    private final FilesFacade filesFacade;
    private final PrintingDataEntityFacade printingDataEntityFacade;

    public Long handleAndInitialPersistPrintingData(@NonNull File triggerFile) {
        return printingDataEntityFacade.initialPersist(triggerFile);
    }

    public boolean handleAndValidatePrintingData(@NonNull Long insertedId) {
        printingDataEntityFacade.validatePrintingDataEntity(insertedId);
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
