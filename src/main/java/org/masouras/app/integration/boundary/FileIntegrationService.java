package org.masouras.app.integration.boundary;

import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import jakarta.annotation.Nullable;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.Validate;
import org.masouras.data.boundary.FilesFacade;
import org.masouras.data.boundary.RepositoryFacade;
import org.masouras.data.control.converter.CsvParser;
import org.masouras.data.control.converter.FileOkAdapter;
import org.masouras.data.domain.FileOkDto;
import org.masouras.data.domain.FileOkRaw;
import org.masouras.model.mssql.schema.jpa.control.entity.enums.ActivityType;
import org.masouras.model.mssql.schema.jpa.control.entity.enums.ContentType;
import org.masouras.model.mssql.schema.jpa.control.entity.enums.FileExtensionType;
import org.masouras.model.mssql.schema.jpa.control.util.EnumUtil;
import org.masouras.trace.annotation.Traceable;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileIntegrationService {
    private final FilesFacade filesFacade;
    private final RepositoryFacade repositoryFacade;

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
        if (EnumUtil.fromCode(FileExtensionType.class, fileOkDto.getFileExtensionType().getCode()) == null) {
            if (log.isWarnEnabled()) log.warn("fileExtensionType {} not found inside FileExtensionType '{}'", fileOkDto.getFileExtensionType().getCode(), fileOk.getName());
            return null;
        }

        if (fileOkDto.getActivityType() == null) {
            if (log.isWarnEnabled()) log.warn("activityType not found inside file '{}'", fileOk.getName());
            return null;
        }
        if (EnumUtil.fromCode(ActivityType.class, fileOkDto.getActivityType().getCode()) == null) {
            if (log.isWarnEnabled()) log.warn("activityType {} not found inside ActivityType '{}'", fileOkDto.getActivityType().getCode(), fileOk.getName());
            return null;
        }

        if (fileOkDto.getContentType() == null) {
            if (log.isWarnEnabled()) log.warn("contentType not found inside file '{}'", fileOk.getName());
            return null;
        }
        if (EnumUtil.fromCode(ContentType.class, fileOkDto.getContentType().getCode()) == null) {
            if (log.isWarnEnabled()) log.warn("contentType {} not found inside ContentType '{}'", fileOkDto.getContentType().getCode(), fileOk.getName());
        }

        return fileOkDto;
    }

    private boolean handleAndPersistFileMain(FileOkDto fileOkDto, File relevantFile) {
        String fileContentBase64 = filesFacade.getContentBase64(relevantFile);
        if (fileContentBase64 == null) return false;

        Long insertedId = repositoryFacade.saveInitialPrintingData(fileOkDto, fileContentBase64);
        if (log.isDebugEnabled()) log.debug("PrintingData Inserted with ID: {}", insertedId);

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
