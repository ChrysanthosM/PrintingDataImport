package org.masouras.strategy;

import java.io.File;

public interface FileProcessorBase {
    FileExtensionType getSupportedExtensionType();
    void process(File file);
    default void fileProcessed(File file) {
        file.delete();
    }
}

