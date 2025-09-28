package org.masouras.strategy;

import lombok.Getter;

public enum FileExtensionType {
    XML("xml"),
    ;

    @Getter
    private final String extension;
    FileExtensionType(String extension) {
        this.extension = extension;
    }

}
