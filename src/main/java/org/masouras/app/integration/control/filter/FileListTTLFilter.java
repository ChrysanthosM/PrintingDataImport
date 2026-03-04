package org.masouras.app.integration.control.filter;

import org.apache.commons.lang3.ArrayUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.integration.file.filters.FileListFilter;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class FileListTTLFilter implements FileListFilter<File> {
    private final Map<String, Long> seen = new ConcurrentHashMap<>();
    private static final long TTL_MILLIS = 30000; // 30 seconds

    @Scheduled(fixedDelay = 30000)
    public void cleanup() {
        long now = System.currentTimeMillis();
        seen.entrySet().removeIf(e -> now - e.getValue() > TTL_MILLIS);
    }

    @Override
    public @NonNull List<File> filterFiles(File[] files) {
        if (ArrayUtils.isEmpty(files)) return Collections.emptyList();
        return Arrays.stream(files)
                .filter(this::accept)
                .collect(Collectors.toList());
    }

    @Override
    public boolean accept(File file) {
        long now = System.currentTimeMillis();

        String key = file.getAbsolutePath();
        Long lastSeen = seen.get(key);
        boolean accepted = (lastSeen == null) || (now - lastSeen > TTL_MILLIS);
        if (accepted) {
            seen.put(key, now);
        }
        return accepted;
    }

    @Override
    public boolean supportsSingleFileFiltering() {
        return false;
    }

    @Override
    public boolean isForRecursion() {
        return false;
    }
}


