package org.masouras.config;

import lombok.extern.slf4j.Slf4j;
import org.masouras.filter.FileExtensionFilter;
import org.masouras.filter.FileLockedFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.file.dsl.Files;
import org.springframework.integration.file.filters.AcceptOnceFileListFilter;
import org.springframework.integration.file.filters.CompositeFileListFilter;

import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.List;

@Configuration
@EnableIntegration
@IntegrationComponentScan
@Slf4j
public class FileIntegrationConfig {
    private static final String WATCH_FOLDER = "D:/MyDocuments/Programming/Files";

    @Bean
    public IntegrationFlow filePollingFlow(FileExtensionFilter fileExtensionFilter, FileLockedFilter fileLockedFilter) {
        return IntegrationFlow
                .from(Files.inboundAdapter(new File(WATCH_FOLDER))
                                .filter(new CompositeFileListFilter<>(List.of(
                                        fileExtensionFilter,
                                        fileLockedFilter,
                                        new AcceptOnceFileListFilter<>())))
                                .preventDuplicates(true),
                        e -> e.poller(Pollers.fixedDelay(2000, 1000)))
                .handle(File.class, (file, headers) -> {
                    handleAndPersistFile(file);
                    return file;
                })
                .handle(File.class, (file, headers) -> {
                    handleAndDeleteFile(file);
                    return null;
                })
                .get();
    }

    private File getXmlFile(File okFile) {
        String baseName = com.google.common.io.Files.getNameWithoutExtension(okFile.getName());
        return new File(okFile.getParentFile(), baseName + ".xml");
    }
    private void handleAndDeleteFile(File okFile) {
        File xmlFile = getXmlFile(okFile);

        boolean xmlDeleted = xmlFile.delete();
        if (log.isDebugEnabled()) log.debug("{} xml deleted:{}", xmlFile.getName(), xmlDeleted);
        if (!xmlDeleted && log.isWarnEnabled()) log.warn("{} xml NOT deleted:", xmlFile.getName());

        boolean okDeleted = okFile.delete();
        if (log.isDebugEnabled()) log.debug("{} ok deleted:{}", okFile.getName(), okDeleted);
        if (!okDeleted && log.isWarnEnabled()) log.warn("{} ok NOT deleted:", okFile.getName());
    }
    private void handleAndPersistFile(File okFile) {
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
}


