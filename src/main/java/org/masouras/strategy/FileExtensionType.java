package org.masouras.strategy;

import lombok.Getter;

public enum FileExtensionType {
    XML("xml"),
    CSV("csv"),
    ;

    @Getter
    private final String extension;
    FileExtensionType(String extension) {
        this.extension = extension;
    }

}
