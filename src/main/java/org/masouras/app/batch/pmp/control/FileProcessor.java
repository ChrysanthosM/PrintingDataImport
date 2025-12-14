package org.masouras.app.batch.pmp.control;

import org.masouras.app.batch.pmp.domain.FileProcessorResult;
import org.masouras.squad.printing.mssql.schema.jpa.control.FileExtensionType;

public interface FileProcessor {
    FileExtensionType getFileExtensionType();
    FileProcessorResult getFileProcessorResult(Object... params);
}
