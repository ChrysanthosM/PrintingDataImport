package org.masouras.app.batch.pmp.control.step.business.parser;

import org.masouras.app.batch.pmp.domain.FileProcessorResult;
import org.masouras.model.mssql.schema.jpa.control.entity.enums.FileExtensionType;

public interface FileProcessor {
    FileExtensionType getFileExtensionType();
    FileProcessorResult getFileProcessorResult(Object... params);
}
