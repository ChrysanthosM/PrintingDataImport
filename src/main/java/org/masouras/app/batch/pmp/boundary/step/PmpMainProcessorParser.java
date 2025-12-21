package org.masouras.app.batch.pmp.boundary.step;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.masouras.app.batch.pmp.control.FileProcessor;
import org.masouras.app.batch.pmp.domain.FileProcessorResult;
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
public class PmpMainProcessorParser implements ItemProcessor<PrintingDataEntity, PrintingDataEntity> {
    private final List<FileProcessor> fileProcessors;
    private final FilesFacade filesFacade;
    private final RepositoryFacade repositoryFacade;

    @Override
    public PrintingDataEntity process(@NotNull PrintingDataEntity printingDataEntity) {
        if (log.isInfoEnabled()) log.info("{}: Parsing printingDataEntity {}", this.getClass().getSimpleName(), printingDataEntity.getId());

        FileProcessorResult fileProcessorResult = fileProcessors.stream()
                .filter(fv -> fv.getFileExtensionType().getCode().equals(printingDataEntity.getFileExtensionType().getCode()))
                .findFirst()
                .orElseThrow(() -> new ValidationException("Parser failed, FileExtensionType not found: " + printingDataEntity.getFileExtensionType().getCode()))
                .getFileProcessorResult(printingDataEntity.getActivity().getActivityType(), printingDataEntity.getContentType(), printingDataEntity.getValidatedContent().getContentBase64());
        if (fileProcessorResult.getStatus() == FileProcessorResult.ProcessorStatus.ERROR) throw new ValidationException("Parser failed with message: " + fileProcessorResult.getMessage());

        return saveContentParsed(printingDataEntity, fileProcessorResult);
    }
    private PrintingDataEntity saveContentParsed(@NonNull PrintingDataEntity printingDataEntity, FileProcessorResult fileProcessorResult) {
        try {
            String stringDocument = filesFacade.documentToString((Document) fileProcessorResult.getResult());
            return repositoryFacade.saveContentParsed(printingDataEntity, filesFacade.stringDocumentToBase64(stringDocument));
        } catch (TransformerException e) {
            log.error("{} failed with message: {}", this.getClass().getSimpleName(), e.getMessage(), e);
            throw new ValidationException("Parser failed with message: " + e.getMessage(), e);
        }
    }
}

