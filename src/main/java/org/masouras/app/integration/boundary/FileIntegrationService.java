package org.masouras.app.integration.boundary;

import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import jakarta.annotation.Nullable;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.Validate;
import org.masouras.data.boundary.FilesFacade;
import org.masouras.data.boundary.RepositoryFacade;
import org.masouras.data.control.CsvParser;
import org.masouras.data.control.FileOkAdapter;
import org.masouras.data.domain.FileOkDto;
import org.masouras.data.domain.FileOkRaw;
import org.masouras.squad.printing.mssql.schema.jpa.control.ActivityType;
import org.masouras.squad.printing.mssql.schema.jpa.control.ContentType;
import org.masouras.squad.printing.mssql.schema.jpa.control.FileExtensionType;
import org.masouras.squad.printing.mssql.schema.jpa.entity.ActivityEntity;
import org.masouras.trace.annotation.Traceable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.List;

@Service
@Slf4j
public class FileIntegrationService {
    private final FilesFacade filesFacade;
    private final RepositoryFacade repositoryFacade;

    @Autowired
    public FileIntegrationService(FilesFacade filesFacade, RepositoryFacade repositoryFacade) {
        this.filesFacade = filesFacade;
        this.repositoryFacade = repositoryFacade;
    }

    @Traceable
    @Timed("handle.and.persist.file")
    @Counted("handle.and.persist.file")
    public boolean handleAndPersistFile(@NonNull File fileOk) {
        FileOkRaw fileOkRaw = getFileOkContent(fileOk);
        if (fileOkRaw == null) {
            if (log.isWarnEnabled()) log.warn("Expected Content inside file {}", fileOk.getName());
            return false;
        }
        FileOkDto fileOkDto = getFileOkDto(fileOkRaw, fileOk);
        if (fileOkDto == null) return false;

        File relevantFile = filesFacade.getRelevantFile(fileOk, fileOkDto);
        if (!relevantFile.exists() || !relevantFile.isFile()) {
            if (log.isWarnEnabled()) log.warn("Expected Relevant file '{}' not found for OK file '{}'", relevantFile.getName(), fileOk.getName());
            return false;
        }

        if (!handleAndPersistFileMain(fileOkDto, relevantFile)) {
            if (log.isWarnEnabled()) log.warn("Relevant file didn't persisted '{}'", relevantFile.getName());
            return false;
        }

        filesFacade.deleteFile(relevantFile);
        if (log.isDebugEnabled()) log.debug("Relevant file persisted '{}'", fileOk.getName());
        return true;
    }
    private @Nullable FileOkDto getFileOkDto(@NonNull FileOkRaw fileOkRaw, @NonNull File fileOk) {
        FileOkDto fileOkDto = FileOkAdapter.toFileOkDto(fileOkRaw);
        if (fileOkDto.getFileExtensionType() == null) {
            if (log.isWarnEnabled()) log.warn("fileExtensionType not found inside file '{}'", fileOk.getName());
            return null;
        }
        if (FileExtensionType.getFromCode(fileOkDto.getFileExtensionType().getCode()) == null) {
            if (log.isWarnEnabled()) log.warn("fileExtensionType {} not found inside FileExtensionType '{}'", fileOkDto.getFileExtensionType().getCode(), fileOk.getName());
            return null;
        }

        if (fileOkDto.getActivityType() == null) {
            if (log.isWarnEnabled()) log.warn("activityType not found inside file '{}'", fileOk.getName());
            return null;
        }
        if (ActivityType.getFromCode(fileOkDto.getActivityType().getCode()) == null) {
            if (log.isWarnEnabled()) log.warn("activityType {} not found inside ActivityType '{}'", fileOkDto.getActivityType().getCode(), fileOk.getName());
            return null;
        }

        if (fileOkDto.getContentType() == null) {
            if (log.isWarnEnabled()) log.warn("contentType not found inside file '{}'", fileOk.getName());
            return null;
        }
        if (ContentType.getFromCode(fileOkDto.getContentType().getCode()) == null) {
            if (log.isWarnEnabled()) log.warn("contentType {} not found inside ContentType '{}'", fileOkDto.getContentType().getCode(), fileOk.getName());
        }

        return fileOkDto;
    }

    @Transactional
    private boolean handleAndPersistFileMain(FileOkDto fileOkDto, File relevantFile) {
        String fileContentBase64 = filesFacade.getContentBase64(relevantFile);
        if (fileContentBase64 == null) return false;

        ActivityEntity activityEntity = repositoryFacade.createActivity(fileOkDto.getActivityType());
        Long insertedId = repositoryFacade.savePrintingData(activityEntity, fileOkDto.getContentType(), fileOkDto.getFileExtensionType(), fileContentBase64);
        if (log.isDebugEnabled()) log.debug("PrintingData Inserted with ID: {} and activity: {}", insertedId, activityEntity.getId());

        return true;
    }

    public @Nullable FileOkRaw getFileOkContent(@NonNull File fileOk) {
        List<FileOkRaw> fileOkRawList = filesFacade.getCsvContent(FileOkRaw.class, CsvParser.DelimiterType.PIPE, fileOk);
        return CollectionUtils.isEmpty(fileOkRawList) ? null : fileOkRawList.getFirst();
    }


    public void handleErrorFile(@NonNull File fileOk, String errorFolder) {
        Validate.notBlank(errorFolder);

        filesFacade.copyFile(fileOk, errorFolder);
        List<String> possibleRelevantFileNames = filesFacade.getPossibleRelevantFileNames(fileOk);
        if (CollectionUtils.isEmpty(possibleRelevantFileNames)) return;
        possibleRelevantFileNames.stream()
                .map(File::new)
                .filter(file -> file.exists() && file.isFile())
                .forEach(file -> filesFacade.moveFile(file, errorFolder));


    }
}
