package org.masouras.strategy;

import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class FileProcessorXML implements FileProcessorBase {
    @Override
    public FileExtensionType getSupportedExtensionType() {
        return FileExtensionType.XML;
    }

    @Override
    public void process(File file) {
        // XML-specific logic
    }
}
