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
public class FileProcessorFactory {
    private final List<FileProcessor> fileProcessors;
    private final Map<String, FileProcessor> fileProcessorMap = new HashMap<>();

    @Nullable
    public FileProcessor getFileProcessor(String fileExtensionType) { return fileProcessorMap.getOrDefault(fileExtensionType, null); }

    @PostConstruct
    private void init() {
        fileProcessors.forEach(fv -> fileProcessorMap.put(fv.getFileExtensionType().name(), fv));
    }
}
