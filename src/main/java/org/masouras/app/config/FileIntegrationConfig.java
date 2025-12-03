package org.masouras.app.config;

import lombok.extern.slf4j.Slf4j;
import org.masouras.app.control.filter.FileExtensionFilter;
import org.masouras.app.boundary.FileIntegrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    @Value("${watch.folder:null}") private String watchFolder;
    @Value("${error.folder:null}") private String errorFolder;

    private final FileIntegrationService fileIntegrationService;

    @Autowired
    public FileIntegrationConfig(FileIntegrationService fileIntegrationService) {
        this.fileIntegrationService = fileIntegrationService;
    }

    @Bean
    public IntegrationFlow okPollingFlow(FileExtensionFilter fileExtensionFilter) {
        return IntegrationFlow
                .from(Files.inboundAdapter(new File(watchFolder))
                                .filter(new CompositeFileListFilter<>(List.of(
                                        fileExtensionFilter,
                                        new AcceptOnceFileListFilter<>()))
                                )
                                .preventDuplicates(true)
                        , e -> e.poller(Pollers.fixedDelay(2000, 1000)))
                .handle(File.class, (file, headers) -> {
                    if (!fileIntegrationService.handleAndPersistFile(file)) fileIntegrationService.handleErrorFile(file, errorFolder);
                    return file;
                })
                .handle(Files.outboundAdapter(new File("archive"))
                        .deleteSourceFiles(true))
                .get();
    }
}


