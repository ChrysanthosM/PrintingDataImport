package org.masouras.app.batch.pmp.boundary.step;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.masouras.app.batch.pmp.control.FileValidator;
import org.masouras.app.batch.pmp.domain.FileValidatorResult;
import org.masouras.data.boundary.FilesFacade;
import org.masouras.data.boundary.RepositoryFacade;
import org.masouras.squad.printing.mssql.schema.jpa.entity.PrintingDataEntity;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.validator.ValidationException;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

import javax.xml.transform.TransformerException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PmpMainProcessorValidation implements ItemProcessor<PrintingDataEntity, PrintingDataEntity> {
    private final List<FileValidator> fileValidators;
    private final FilesFacade filesFacade;
    private final RepositoryFacade repositoryFacade;

    @Override
    public PrintingDataEntity process(@NotNull PrintingDataEntity printingDataEntity) {
        if (log.isInfoEnabled()) log.info("{}: Validating printingDataEntity {}", this.getClass().getSimpleName(), printingDataEntity.getId());

        FileValidatorResult fileValidatorResult = fileValidators.stream()
                .filter(fv -> fv.getFileExtensionType().getCode().equals(printingDataEntity.getFileExtensionType().getCode()))
                .findFirst()
                .orElseThrow(() -> new ValidationException("Validation failed, FileExtensionType not found: " + printingDataEntity.getFileExtensionType().getCode()))
                .getValidatedResult(printingDataEntity.getInitialContent().getContentBase64());
        if (fileValidatorResult.getStatus() == FileValidatorResult.ValidationStatus.ERROR) throw new ValidationException("Validation failed with message: " + fileValidatorResult.getMessage());

        return saveContentValidated(printingDataEntity, fileValidatorResult);
    }

    private PrintingDataEntity saveContentValidated(@NonNull PrintingDataEntity printingDataEntity, FileValidatorResult fileValidatorResult) {
        try {
            String stringDocument = filesFacade.documentToString((Document) fileValidatorResult.getResult());
            return repositoryFacade.saveContentValidated(printingDataEntity, filesFacade.stringDocumentToBase64(stringDocument));
        } catch (TransformerException e) {
            log.error("{} failed with message: {}", this.getClass().getSimpleName(), e.getMessage(), e);
            throw new ValidationException("Validation failed with message: " + e.getMessage(), e);
        }
    }
}

