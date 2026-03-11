package org.masouras.app.integration.control.domain;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.io.File;

@RequiredArgsConstructor
@Data
public class FileProcessingState {
    private final File file;
    private final Long insertedId;
}
