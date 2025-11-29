package org.masouras.config;

import lombok.extern.slf4j.Slf4j;
import org.masouras.filter.FileExtensionFilter;
import org.masouras.process.FileIntegrationControl;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.List;

@Configuration
@EnableIntegration
@IntegrationComponentScan
@Slf4j
public class FileIntegrationConfig {
    private static final String WATCH_FOLDER = "D:/MyDocuments/Programming/Files";

    private final FileIntegrationControl fileIntegrationControl;

    @Autowired
    public FileIntegrationConfig(FileIntegrationControl fileIntegrationControl) {
        this.fileIntegrationControl = fileIntegrationControl;
    }

    @Bean
    public IntegrationFlow filePollingFlow(FileExtensionFilter fileExtensionFilter) {
        return IntegrationFlow
                .from(Files.inboundAdapter(new File(WATCH_FOLDER))
                                .filter(new CompositeFileListFilter<>(List.of(
                                        fileExtensionFilter,
                                        new AcceptOnceFileListFilter<>())))
                                .preventDuplicates(true),
                        e -> e.poller(Pollers.fixedDelay(2000, 1000)))
                .handle(File.class, (file, headers) -> {
                    fileIntegrationControl.handleAndPersistFile(file);
                    return file;
                })
                .handle(Files.outboundAdapter(new File("archive"))
                        .deleteSourceFiles(true))
                .get();
    }
}


