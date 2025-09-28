package org.masouras.process;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Base64;

@Service
@Slf4j
public class FileIntegrationHandler {

    public void handleAndDeleteFile(File okFile) {
        File xmlFile = getXmlFile(okFile);

        boolean xmlDeleted = xmlFile.delete();
        if (log.isDebugEnabled()) log.debug("{} xml deleted:{}", xmlFile.getName(), xmlDeleted);
        if (!xmlDeleted && log.isWarnEnabled()) log.warn("{} xml NOT deleted:", xmlFile.getName());

        boolean okDeleted = okFile.delete();
        if (log.isDebugEnabled()) log.debug("{} ok deleted:{}", okFile.getName(), okDeleted);
        if (!okDeleted && log.isWarnEnabled()) log.warn("{} ok NOT deleted:", okFile.getName());
    }

    public void handleAndPersistFile(File okFile) {
        try {
            File xmlFile = getXmlFile(okFile);
            if (xmlFile.exists()) {
                String xmlContentBase64 = Base64.getEncoder().encodeToString(java.nio.file.Files.readAllBytes(xmlFile.toPath()));
//                fileRepository.save(new FileEntity(file.getName(), extension, fileContentBase64));

                if (log.isInfoEnabled()) log.info("Saved XML file '{}' to database", xmlFile.getName());
            } else {
                if (log.isWarnEnabled()) log.warn("Expected XML file '{}' not found for OK file '{}'", xmlFile.getName(), okFile.getName());
            }

        } catch (IOException e) {
            log.error("Failed to read or save file: {}", okFile.getAbsolutePath(), e);
        }
    }

    private File getXmlFile(File okFile) {
        String baseName = com.google.common.io.Files.getNameWithoutExtension(okFile.getName());
        return new File(okFile.getParentFile(), baseName + ".xml");
    }
}
