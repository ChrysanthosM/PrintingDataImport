package org.masouras.process;

import lombok.extern.slf4j.Slf4j;
import org.masouras.config.FileExtensionType;
import org.masouras.data.service.FileOnDBActions;
import org.masouras.data.service.FileOnDiscActions;
import org.masouras.printing.sqlite.schema.entity.ActivityEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;

@Service
@Slf4j
public class FileIntegrationControl {
    private final FileOnDiscActions fileOnDiscActions;
    private final FileOnDBActions fileOnDBActions;

    @Autowired
    public FileIntegrationControl(FileOnDiscActions fileOnDiscActions, FileOnDBActions fileOnDBActions) {
        this.fileOnDiscActions = fileOnDiscActions;
        this.fileOnDBActions = fileOnDBActions;
    }

    public boolean handleAndPersistFile(File okFile, FileExtensionType fileExtensionType) {
        File relevantFile = fileOnDiscActions.getRelevantFile(okFile, fileExtensionType);
        if (!relevantFile.exists()) {
            if (log.isWarnEnabled()) log.warn("Expected Relevant file '{}' not found for OK file '{}'", relevantFile.getName(), okFile.getName());
            return false;
        }
        return handleAndPersistFileMain(fileExtensionType, relevantFile);
    }
    @Transactional
    private boolean handleAndPersistFileMain(FileExtensionType fileExtensionType, File relevantFile) {
        String fileContentBase64 = fileOnDiscActions.getContentBase64(relevantFile);
        if (fileContentBase64 == null) return false;

        ActivityEntity activityEntity = fileOnDBActions.createActivity(fileExtensionType);
        Long insertedId = fileOnDBActions.savePrintingData(activityEntity, relevantFile, fileContentBase64);
        if (log.isDebugEnabled()) log.debug("PrintingData Inserted with ID: {} and activity: {}", insertedId, activityEntity.getId());

        return true;
    }

    public void handleAndDeleteFile(File okFile, FileExtensionType fileExtensionType) {
        File relevantFile = fileOnDiscActions.getRelevantFile(okFile, fileExtensionType);
        fileOnDiscActions.deleteFile(relevantFile);
        fileOnDiscActions.deleteFile(okFile);
    }
}
