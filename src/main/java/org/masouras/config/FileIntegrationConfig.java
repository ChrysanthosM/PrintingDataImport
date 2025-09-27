package org.masouras.config;

import lombok.extern.slf4j.Slf4j;
import org.masouras.model.FileExtensionFilter;
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
    public IntegrationFlow filePollingFlow(FileExtensionFilter fileExtensionFilter) {
        return IntegrationFlow
                .from(Files.inboundAdapter(new File(WATCH_FOLDER))
                                .filter(new CompositeFileListFilter<>(List.of(new AcceptOnceFileListFilter<>(), fileExtensionFilter)))
                                .nioLocker()
                                .preventDuplicates(true),
                        e -> e.poller(Pollers.fixedDelay(2000, 1000)))
                .handle(File.class, (file, headers) -> {
                    handleAndPersistFile(file);
                    return null;
                })
                .get();
    }


    private void handleAndPersistFile(File file) {
        try {
            String fileContentBase64 = Base64.getEncoder().encodeToString(java.nio.file.Files.readAllBytes(file.toPath()));


//                fileRepository.save(new FileEntity(file.getName(), extension, fileContentBase64));

            log.info("Saved file '{}' to database", file.getName());
        } catch (IOException e) {
            log.error("Failed to read or save file: {}", file.getAbsolutePath(), e);
        }
    }
}


