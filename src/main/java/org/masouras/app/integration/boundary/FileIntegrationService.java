package org.masouras.app.integration.boundary;

import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import jakarta.annotation.Nullable;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.Validate;
import org.masouras.control.converter.CsvParser;
import org.masouras.control.converter.TriggerFileAdapter;
import org.masouras.domain.TriggerFileDto;
import org.masouras.domain.TriggerFileRaw;
import org.masouras.facade.FilesFacade;
import org.masouras.facade.PrintingDataEntityFacade;
import org.masouras.facade.RepositoryFacade;
import org.masouras.model.mssql.schema.jpa.control.entity.enums.ActivityType;
import org.masouras.model.mssql.schema.jpa.control.entity.enums.ContentType;
import org.masouras.model.mssql.schema.jpa.control.entity.enums.FileExtensionType;
import org.masouras.model.mssql.schema.jpa.control.entity.enums.PrintingWayType;
import org.masouras.model.mssql.schema.jpa.control.util.EnumUtil;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileIntegrationService {
    private final FilesFacade filesFacade;
    private final RepositoryFacade repositoryFacade;
    private final PrintingDataEntityFacade printingDataEntityFacade;

    @Timed("handle.and.persist.initial.printing.data")
    @Counted("handle.and.persist.initial.printing.data")
    public Long handleAndPersistInitialPrintingData(@NonNull File triggerFile) {
        TriggerFileRaw triggerFileRaw = getTriggerFileContent(triggerFile);
        if (triggerFileRaw == null) {
            if (log.isWarnEnabled()) log.warn("Expected Content inside file {}", triggerFile.getName());
            return null;
        }
        TriggerFileDto triggerFileDto = getTriggerFileDto(triggerFileRaw, triggerFile);
        if (triggerFileDto == null) return null;

        File relevantFile = filesFacade.getRelevantFile(triggerFile, triggerFileDto);
        if (!relevantFile.exists() || !relevantFile.isFile()) {
            if (log.isWarnEnabled()) log.warn("Expected Relevant file '{}' not found for Trigger file '{}'", relevantFile.getName(), triggerFile.getName());
            return null;
        }

        Long insertedId = handleAndPersistInitialPrintingDataMain(triggerFileDto, relevantFile);
        if (insertedId == null) {
            if (log.isWarnEnabled()) log.warn("Relevant file didn't persisted '{}'", relevantFile.getName());
            return null;
        }

        filesFacade.deleteFile(relevantFile);
        if (log.isDebugEnabled()) log.debug("Relevant file persisted '{}'", triggerFile.getName());
        return insertedId;
    }
    private @Nullable TriggerFileDto getTriggerFileDto(@NonNull TriggerFileRaw triggerFileRaw, @NonNull File triggerFile) {
        Optional<PrintingWayType> printingWayType = Arrays.stream(PrintingWayType.values())
                .filter(way -> way.name().equals(FilenameUtils.getExtension(triggerFile.getName().toUpperCase()))).findFirst();
        if (printingWayType.isEmpty()) {
            if (log.isWarnEnabled()) log.warn("PrintingWayType not found for file '{}'", triggerFile.getName());
            return null;
        }

        TriggerFileDto triggerFileDto = TriggerFileAdapter.toTriggerFileDto(triggerFileRaw, printingWayType.get());
        if (triggerFileDto.getFileExtensionType() == null) {
            if (log.isWarnEnabled()) log.warn("fileExtensionType not found inside file '{}'", triggerFile.getName());
            return null;
        }
        if (EnumUtil.fromCode(FileExtensionType.class, triggerFileDto.getFileExtensionType().getCode()) == null) {
            if (log.isWarnEnabled()) log.warn("fileExtensionType {} not found inside FileExtensionType '{}'", triggerFileDto.getFileExtensionType().getCode(), triggerFile.getName());
            return null;
        }

        if (triggerFileDto.getActivityType() == null) {
            if (log.isWarnEnabled()) log.warn("activityType not found inside file '{}'", triggerFile.getName());
            return null;
        }
        if (EnumUtil.fromCode(ActivityType.class, triggerFileDto.getActivityType().getCode()) == null) {
            if (log.isWarnEnabled()) log.warn("activityType {} not found inside ActivityType '{}'", triggerFileDto.getActivityType().getCode(), triggerFile.getName());
            return null;
        }

        if (triggerFileDto.getContentType() == null) {
            if (log.isWarnEnabled()) log.warn("contentType not found inside file '{}'", triggerFile.getName());
            return null;
        }
        if (EnumUtil.fromCode(ContentType.class, triggerFileDto.getContentType().getCode()) == null) {
            if (log.isWarnEnabled()) log.warn("contentType {} not found inside ContentType '{}'", triggerFileDto.getContentType().getCode(), triggerFile.getName());
        }

        return triggerFileDto;
    }

    private Long handleAndPersistInitialPrintingDataMain(TriggerFileDto triggerFileDto, File relevantFile) {
        Optional<byte[]> fileContent = filesFacade.getContentBytes(relevantFile);
        if (fileContent.isEmpty()) return null;

        Long insertedId = repositoryFacade.saveInitialPrintingData(triggerFileDto, fileContent.get());
        if (log.isInfoEnabled()) log.info("PrintingData Inserted with ID: {}", insertedId);
        return insertedId;
    }

    public @Nullable TriggerFileRaw getTriggerFileContent(@NonNull File triggerFile) {
        List<TriggerFileRaw> triggerFileRawList = filesFacade.getCsvContent(TriggerFileRaw.class, CsvParser.DelimiterType.PIPE, triggerFile);
        return CollectionUtils.isEmpty(triggerFileRawList) ? null : triggerFileRawList.getFirst();
    }

    @Timed("handle.and.validate.printing.data")
    @Counted("handle.and.validate.printing.data")
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
