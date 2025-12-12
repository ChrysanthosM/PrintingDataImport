package org.masouras.app.batch.pmp.control.business.control.step;

import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.masouras.app.batch.pmp.control.business.control.validator.FileValidator;
import org.masouras.squad.printing.mssql.schema.jpa.entity.PrintingDataEntity;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.validator.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class PmpMainProcessorValidation implements ItemProcessor<PrintingDataEntity, PrintingDataEntity> {
    private final List<FileValidator> fileValidators;

    @Autowired
    public PmpMainProcessorValidation(List<FileValidator> fileValidators) {
        this.fileValidators = fileValidators;
    }

    @Override
    public PrintingDataEntity process(@NotNull PrintingDataEntity printingDataEntity) {
        if (log.isInfoEnabled()) log.info("{}: Validating printingDataEntity {}", this.getClass().getSimpleName(), printingDataEntity.getId());

        String validatedOK = fileValidators.stream()
                .filter(fv -> fv.getFileExtensionType().equals(printingDataEntity.getFileExtensionType()))
                .findFirst().orElseThrow()
                .validate(printingDataEntity.getContentBase64());
        if (StringUtils.isNotBlank(validatedOK)) throw new ValidationException("Validation failed with message: " + validatedOK);

        return printingDataEntity;
    }
}

