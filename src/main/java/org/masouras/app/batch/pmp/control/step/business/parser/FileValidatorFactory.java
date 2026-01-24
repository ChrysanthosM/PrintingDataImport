package org.masouras.app.batch.pmp.control.step.business.parser;

import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class FileValidatorFactory {
    private final List<FileValidator> fileValidators;
    private final Map<String, FileValidator> fileValidatorMap = new HashMap<>();

    @Nullable
    public FileValidator getFileValidator(String fileExtensionType) { return fileValidatorMap.getOrDefault(fileExtensionType, null); }

    @PostConstruct
    private void init() {
        fileValidators.forEach(fv -> fileValidatorMap.put(fv.getFileExtensionType().name(), fv));
    }
}
