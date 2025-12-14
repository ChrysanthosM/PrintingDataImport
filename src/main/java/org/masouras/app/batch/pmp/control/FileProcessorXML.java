package org.masouras.app.batch.pmp.control;

import lombok.extern.slf4j.Slf4j;
import org.masouras.app.batch.pmp.domain.FileProcessorResult;
import org.masouras.squad.printing.mssql.schema.jpa.control.FileExtensionType;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class FileProcessorXML implements FileProcessor {

    @Override
    public FileExtensionType getFileExtensionType() {
        return FileExtensionType.XML;
    }

    @Override
    public FileProcessorResult getFileProcessorResult(Object... params) {
        return null;
    }
}
