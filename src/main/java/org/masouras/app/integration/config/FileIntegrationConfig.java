package org.masouras.app.integration.config;

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.masouras.app.integration.boundary.FileIntegrationService;
import org.masouras.app.integration.control.filter.FileExtensionFilter;
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
@RequiredArgsConstructor
public class FileIntegrationConfig {
    @Value("${watch.folder:#{null}}") private String watchFolder;
    @Value("${error.folder:#{null}}") private String errorFolder;
    @Value("${archive.folder:#{null}}") private String archiveFolder;

    private final FileIntegrationService fileIntegrationService;

    @Bean
    public IntegrationFlow batchPollingFlow(FileExtensionFilter fileExtensionFilter) {
        Preconditions.checkNotNull(watchFolder, "watch.folder property must be set");
        Preconditions.checkNotNull(errorFolder, "error.folder property must be set");
        Preconditions.checkNotNull(archiveFolder, "archive.folder property must be set");

        return IntegrationFlow
                .from(Files.inboundAdapter(new File(watchFolder))
                                .filter(new CompositeFileListFilter<>(List.of(
                                        fileExtensionFilter
//                                        new AcceptOnceFileListFilter<>()
                                        ))
                                )
                                .preventDuplicates(false)
                        , e -> e.poller(Pollers.fixedDelay(2000, 1000)))
                .handle(File.class, (file, _) -> {
                    if (!fileIntegrationService.handleAndPersistFile(file)) fileIntegrationService.handleErrorFile(file, errorFolder);
                    return file;
                })
                .handle(Files.outboundAdapter(new File(archiveFolder))
                        .deleteSourceFiles(true))
                .get();
    }
}


