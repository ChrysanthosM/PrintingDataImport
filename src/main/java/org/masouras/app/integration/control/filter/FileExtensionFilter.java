package org.masouras.app.integration.control.filter;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.jspecify.annotations.NonNull;
import org.masouras.model.mssql.schema.jpa.control.entity.enums.PrintingWayType;
import org.springframework.integration.file.filters.FileListFilter;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class FileExtensionFilter implements FileListFilter<File> {
    private final Set<String> ALLOWED_EXTENSIONS = Set.of(
            PrintingWayType.BATCH.name().toLowerCase(),
            PrintingWayType.ARTEMIS.name().toLowerCase());

    @Override
    public @NonNull List<File> filterFiles(File[] files) {
        if (ArrayUtils.isEmpty(files)) return Collections.emptyList();
        return Arrays.stream(files)
                .filter(this::accept)
                .collect(Collectors.toList());
    }

    @Override
    public boolean accept(File file) {
        String ext = FilenameUtils.getExtension(file.getName()).toLowerCase();
        return ALLOWED_EXTENSIONS.contains(ext);
    }

    @Override
    public boolean supportsSingleFileFiltering() {
        return true;
    }

    @Override
    public boolean isForRecursion() {
        return false;
    }
}


