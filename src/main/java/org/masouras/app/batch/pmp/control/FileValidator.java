package org.masouras.app.batch.pmp.control;

import org.masouras.app.batch.pmp.domain.FileValidatorResult;
import org.masouras.model.mssql.schema.jpa.control.entity.enums.FileExtensionType;

public interface FileValidator {
    FileExtensionType getFileExtensionType();
    FileValidatorResult getValidatedResult(Object... params);
}
