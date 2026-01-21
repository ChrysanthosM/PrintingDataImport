package org.masouras.app.batch.pmp.boundary.step;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.masouras.app.batch.pmp.control.FileProcessor;
import org.masouras.app.batch.pmp.control.FileProcessorFactory;
import org.masouras.app.batch.pmp.domain.FileProcessorResult;
import org.masouras.data.boundary.FilesFacade;
import org.masouras.data.boundary.RepositoryFacade;
import org.masouras.model.mssql.schema.jpa.control.entity.PrintingDataEntity;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.validator.ValidationException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PmpMainProcessorParser implements ItemProcessor<PrintingDataEntity, PrintingDataEntity> {
    private final FileProcessorFactory fileProcessorFactory;
    private final FilesFacade filesFacade;
    private final RepositoryFacade repositoryFacade;

    @Override
    public PrintingDataEntity process(@NotNull PrintingDataEntity printingDataEntity) {
        if (log.isInfoEnabled()) log.info("{}: Parsing printingDataEntity {}", this.getClass().getSimpleName(), printingDataEntity.getId());

        FileProcessor fileProcessor = fileProcessorFactory.getFileProcessor(printingDataEntity.getFileExtensionType().getCode());
        if (fileProcessor == null) { throw new ValidationException("Parser failed, FileExtensionType not found: " + printingDataEntity.getFileExtensionType().getCode()); }
        FileProcessorResult fileProcessorResult = fileProcessor.getFileProcessorResult(printingDataEntity.getActivity().getActivityType(), printingDataEntity.getContentType(), printingDataEntity.getValidatedContent().getContentBase64());
        if (fileProcessorResult.getStatus() == FileProcessorResult.ProcessorStatus.ERROR) throw new ValidationException("Parser failed with message: " + fileProcessorResult.getMessage());

        return saveContentParsed(printingDataEntity, fileProcessorResult);
    }
    private PrintingDataEntity saveContentParsed(@NonNull PrintingDataEntity printingDataEntity, FileProcessorResult fileProcessorResult) {
        return repositoryFacade.saveContentParsed(printingDataEntity, filesFacade.objectToBase64(fileProcessorResult.getResult()));
    }
}

