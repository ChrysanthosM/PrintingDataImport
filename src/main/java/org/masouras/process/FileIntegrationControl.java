package org.masouras.process;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.masouras.config.FileExtensionType;
import org.masouras.data.control.CsvParser;
import org.masouras.data.domain.FileOkRecord;
import org.masouras.data.service.FileOnDBActions;
import org.masouras.data.service.FileOnDiscActions;
import org.masouras.printing.sqlite.schema.entity.ActivityEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.List;

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

    public void handleAndPersistFile(@NonNull File okFile) {
        FileOkRecord fileOkRecord = getFileOkContent(okFile);
        if (fileOkRecord == null) {
            if (log.isWarnEnabled()) log.warn("Expected Content inside file {}", okFile.getName());
            return;
        }
        FileExtensionType fileExtensionType = FileExtensionType.getFormExtension(fileOkRecord.getRelevantFileExtension());
        if (fileExtensionType == null) {
            if (log.isWarnEnabled()) log.warn("fileExtensionType not found inside file '{}'", okFile.getName());
            return;
        }

        File relevantFile = fileOnDiscActions.getRelevantFile(okFile, fileExtensionType);
        if (!relevantFile.exists() || !relevantFile.isFile()) {
            if (log.isWarnEnabled()) log.warn("Expected Relevant file '{}' not found for OK file '{}'", relevantFile.getName(), okFile.getName());
            return;
        }

        if (!handleAndPersistFileMain(fileExtensionType, relevantFile)) {
            if (log.isWarnEnabled()) log.warn("Relevant file didn't persisted '{}'", relevantFile.getName());
            return;
        }

        fileOnDiscActions.deleteFile(relevantFile);
    }
    @Transactional
    private boolean handleAndPersistFileMain(@NonNull FileExtensionType fileExtensionType, @NonNull File relevantFile) {
        String fileContentBase64 = fileOnDiscActions.getContentBase64(relevantFile);
        if (fileContentBase64 == null) return false;

        ActivityEntity activityEntity = fileOnDBActions.createActivity(fileExtensionType);
        Long insertedId = fileOnDBActions.savePrintingData(activityEntity, relevantFile, fileContentBase64);
        if (log.isDebugEnabled()) log.debug("PrintingData Inserted with ID: {} and activity: {}", insertedId, activityEntity.getId());

        return true;
    }

    public FileOkRecord getFileOkContent(@NonNull File fileOk) {
        List<FileOkRecord> fileOkRecords = fileOnDiscActions.getCsvContent(FileOkRecord.class, fileOk, CsvParser.DelimiterType.PIPE);
        return CollectionUtils.isEmpty(fileOkRecords) ? null : fileOkRecords.getFirst();
    }
}
