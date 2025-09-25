package org.masouras.strategy;

import java.io.File;

public interface FileProcessorBase {
    FileExtensionType getSupportedExtensionType();
    void process(File file);
}

