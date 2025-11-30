package org.masouras.process;

import jakarta.annotation.Nullable;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.masouras.data.control.CsvParser;
import org.masouras.data.control.FileOkAdapter;
import org.masouras.data.domain.FileOkDto;
import org.masouras.data.domain.FileOkRaw;
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
        FileOkRaw fileOkRaw = getFileOkContent(okFile);
        if (fileOkRaw == null) {
            if (log.isWarnEnabled()) log.warn("Expected Content inside file {}", okFile.getName());
            return;
        }
        FileOkDto fileOkDto = getFileOkDto(fileOkRaw, okFile);
        if (fileOkDto == null) return;

        File relevantFile = fileOnDiscActions.getRelevantFile(okFile, fileOkDto);
        if (!relevantFile.exists() || !relevantFile.isFile()) {
            if (log.isWarnEnabled()) log.warn("Expected Relevant file '{}' not found for OK file '{}'", relevantFile.getName(), okFile.getName());
            return;
        }

        if (!handleAndPersistFileMain(fileOkDto, relevantFile)) {
            if (log.isWarnEnabled()) log.warn("Relevant file didn't persisted '{}'", relevantFile.getName());
            return;
        }

        fileOnDiscActions.deleteFile(relevantFile);
    }
    private @Nullable FileOkDto getFileOkDto(@NonNull FileOkRaw fileOkRaw, @NonNull File okFile) {
        FileOkDto fileOkDto = FileOkAdapter.toFileOkDto(fileOkRaw);
        if (fileOkDto.getFileExtensionType() == null) {
            if (log.isWarnEnabled()) log.warn("fileExtensionType not found inside file '{}'", okFile.getName());
            return null;
        }
        if (fileOkDto.getActivityType() == null) {
            if (log.isWarnEnabled()) log.warn("activityType not found inside file '{}'", okFile.getName());
            return null;
        }
        if (fileOkDto.getContentType() == null) {
            if (log.isWarnEnabled()) log.warn("contentType not found inside file '{}'", okFile.getName());
            return null;
        }
        return fileOkDto;
    }

    @Transactional
    private boolean handleAndPersistFileMain(@NonNull FileOkDto fileOkDto, @NonNull File relevantFile) {
        String fileContentBase64 = fileOnDiscActions.getContentBase64(relevantFile);
        if (fileContentBase64 == null) return false;

        ActivityEntity activityEntity = fileOnDBActions.createActivity(fileOkDto.getActivityType());
        Long insertedId = fileOnDBActions.savePrintingData(activityEntity, fileOkDto.getContentType(), fileContentBase64);
        if (log.isDebugEnabled()) log.debug("PrintingData Inserted with ID: {} and activity: {}", insertedId, activityEntity.getId());

        return true;
    }

    public FileOkRaw getFileOkContent(@NonNull File fileOk) {
        List<FileOkRaw> fileOkRawList = fileOnDiscActions.getCsvContent(FileOkRaw.class, fileOk, CsvParser.DelimiterType.PIPE);
        return CollectionUtils.isEmpty(fileOkRawList) ? null : fileOkRawList.getFirst();
    }
}
