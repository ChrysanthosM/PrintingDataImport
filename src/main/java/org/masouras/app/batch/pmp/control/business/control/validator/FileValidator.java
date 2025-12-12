package org.masouras.app.batch.pmp.control.business.control.validator;

import org.masouras.squad.printing.mssql.schema.jpa.control.FileExtensionType;

public interface FileValidator {
    FileExtensionType getFileExtensionType();
    String validate(Object... params);
}
