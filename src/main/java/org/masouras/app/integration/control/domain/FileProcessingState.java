package org.masouras.app.integration.control.domain;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.masouras.model.mssql.schema.jpa.control.entity.PrintingDataEntity;

import java.io.File;

@RequiredArgsConstructor
@Data
public class FileProcessingState {
    private final File file;
    private final Long insertedId;

    private PrintingDataEntity printingDataEntity;
}
