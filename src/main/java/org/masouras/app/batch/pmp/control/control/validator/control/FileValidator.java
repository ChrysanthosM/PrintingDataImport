package org.masouras.app.batch.pmp.control.control.validator.control;

import org.masouras.app.batch.pmp.control.control.validator.domain.FileValidatorResult;
import org.masouras.squad.printing.mssql.schema.jpa.control.FileExtensionType;

public interface FileValidator {
    FileExtensionType getFileExtensionType();
    FileValidatorResult getValidatedResult(Object... params);
}
