package org.masouras.process;

import com.google.common.io.Files;
import lombok.extern.slf4j.Slf4j;
import org.masouras.FileExtensionType;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Base64;

@Service
@Slf4j
public class FileOnDiscActions {

    @Cacheable
    public File getRelevantFile(File okFile, FileExtensionType fileExtensionType) {
        String baseName = Files.getNameWithoutExtension(okFile.getName());
        return new File(okFile.getParentFile(), baseName + "." + fileExtensionType.getExtension());
    }

    public String getContentBase64(File fromFile) {
        try {
            return Base64.getEncoder().encodeToString(java.nio.file.Files.readAllBytes(fromFile.toPath()));
        } catch (IOException e) {
            log.error("Failed to read relevant File: {}", fromFile.getAbsolutePath(), e);
            return null;
        }
    }

    public void deleteFile(File file) {
        boolean fileDeleted = file.delete();
        if (log.isDebugEnabled()) log.debug("{} file deleted:{}", file.getName(), fileDeleted);
        if (!fileDeleted && log.isWarnEnabled()) log.warn("{} file NOT deleted:", file.getName());
    }

}
