package org.masouras.strategy;

import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class FileProcessorCSV implements FileProcessorBase {
    @Override
    public FileExtensionType getSupportedExtensionType() {
        return FileExtensionType.CSV;
    }

    @Override
    public void process(File file) {
        // CSV-specific logic
    }
}
