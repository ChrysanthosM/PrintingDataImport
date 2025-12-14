package org.masouras.app.batch.pmp.control.control.step;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.masouras.app.batch.pmp.control.control.validator.control.FileValidator;
import org.masouras.app.batch.pmp.control.control.validator.domain.FileValidatorResult;
import org.masouras.data.boundary.FilesFacade;
import org.masouras.data.boundary.RepositoryFacade;
import org.masouras.squad.printing.mssql.schema.jpa.control.PrintingStatus;
import org.masouras.squad.printing.mssql.schema.jpa.entity.PrintingDataEntity;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.validator.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

import javax.xml.transform.TransformerException;
import java.util.List;

@Slf4j
@Service
public class PmpMainProcessorValidation implements ItemProcessor<PrintingDataEntity, PrintingDataEntity> {
    private final List<FileValidator> fileValidators;
    private final FilesFacade filesFacade;
    private final RepositoryFacade repositoryFacade;

    @Autowired
    public PmpMainProcessorValidation(List<FileValidator> fileValidators, FilesFacade filesFacade, RepositoryFacade repositoryFacade) {
        this.fileValidators = fileValidators;
        this.filesFacade = filesFacade;
        this.repositoryFacade = repositoryFacade;
    }

    @Override
    public PrintingDataEntity process(@NotNull PrintingDataEntity printingDataEntity) {
        if (log.isInfoEnabled()) log.info("{}: Validating printingDataEntity {}", this.getClass().getSimpleName(), printingDataEntity.getId());

        FileValidatorResult fileValidatorResult = fileValidators.stream()
                .filter(fv -> fv.getFileExtensionType().getCode().equals(printingDataEntity.getFileExtensionType().getCode()))
                .findFirst()
                .orElseThrow(() -> new ValidationException("Validation failed, FileExtensionType not found: " + printingDataEntity.getFileExtensionType().getCode()))
                .gatValidatedResult(printingDataEntity.getContentBase64());
        if (fileValidatorResult.getValidationStatus() == FileValidatorResult.ValidationStatus.ERROR) throw new ValidationException("Validation failed with message: " + fileValidatorResult.getValidationMessage());

        return saveContentValidated(printingDataEntity, fileValidatorResult);
    }

    private PrintingDataEntity saveContentValidated(@NonNull PrintingDataEntity printingDataEntity, FileValidatorResult fileValidatorResult) {
        try {
            String stringDocument = filesFacade.documentToString((Document) fileValidatorResult.getValidationResult());
            return repositoryFacade.saveContentValidated(printingDataEntity, filesFacade.stringDocumentToBase64(stringDocument));
        } catch (TransformerException e) {
            log.error("{} failed with message: {}", this.getClass().getSimpleName(), e.getMessage(), e);
            throw new ValidationException("Validation failed with message: " + e.getMessage(), e);
        }
    }
}

