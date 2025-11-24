package org.masouras.process;

import lombok.extern.slf4j.Slf4j;
import org.masouras.config.FileExtensionType;
import org.masouras.data.service.FileOnDBActions;
import org.masouras.data.service.FileOnDiscActions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        return handleAndPersistFileMain(okFile, relevantFile);
    }
    private boolean handleAndPersistFileMain(File okFile, File relevantFile) {
        String xmlContentBase64 = fileOnDiscActions.getContentBase64(relevantFile);
        if (xmlContentBase64 == null) return false;

        fileOnDBActions.createActivity();


//        fileRepository.save(new FileEntity(file.getName(), extension, fileContentBase64));
        return true;
    }

    public void handleAndDeleteFile(File okFile, FileExtensionType fileExtensionType) {
        File relevantFile = fileOnDiscActions.getRelevantFile(okFile, fileExtensionType);
        fileOnDiscActions.deleteFile(relevantFile);
        fileOnDiscActions.deleteFile(okFile);
    }
}
