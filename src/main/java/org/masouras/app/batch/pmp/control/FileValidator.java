package org.masouras.app.batch.pmp.control;

import org.masouras.app.batch.pmp.domain.FileValidatorResult;
import org.masouras.squad.printing.mssql.schema.jpa.control.FileExtensionType;

public interface FileValidator {
    FileExtensionType getFileExtensionType();
    FileValidatorResult getValidatedResult(Object... params);
}
