package org.masouras.app.integration.config;

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.aop.Advice;
import org.masouras.app.integration.boundary.FileIntegrationService;
import org.masouras.app.integration.control.domain.FileProcessingState;
import org.masouras.app.integration.control.filter.FileExtensionFilter;
import org.masouras.app.integration.control.filter.FileListTTLFilter;
import org.masouras.model.mssql.schema.jpa.control.entity.PrintingDataEntity;
import org.masouras.model.mssql.schema.jpa.control.entity.enums.PrintingStatus;
import org.masouras.model.mssql.schema.jpa.control.entity.enums.PrintingWayType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.core.GenericHandler;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.file.dsl.Files;
import org.springframework.integration.file.filters.CompositeFileListFilter;
import org.springframework.retry.interceptor.RetryInterceptorBuilder;

import java.io.File;
import java.util.List;
import java.util.concurrent.Executors;

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
    public IntegrationFlow batchPollingFlow(FileExtensionFilter fileExtensionFilter, FileListTTLFilter fileListTTLFilter) {
        Preconditions.checkNotNull(watchFolder, "watch.folder property must be set");
        Preconditions.checkNotNull(errorFolder, "error.folder property must be set");
        Preconditions.checkNotNull(archiveFolder, "archive.folder property must be set");

        return IntegrationFlow
                .from(Files.inboundAdapter(new File(watchFolder))
                                .filter(new CompositeFileListFilter<>(
                                        List.of(
                                                fileExtensionFilter,
                                                fileListTTLFilter
                                        )
                                ))
                                .preventDuplicates(false),
                        e -> e.poller(Pollers.fixedDelay(1000).maxMessagesPerPoll(100)))

                .channel(MessageChannels.executor(Executors.newFixedThreadPool(10)))
                .handle(fileProcessorInitial(fileIntegrationService, errorFolder))

                .channel(MessageChannels.executor(Executors.newFixedThreadPool(10)))
                .handle(fileProcessorValidate(fileIntegrationService, errorFolder))

                .channel(MessageChannels.executor(Executors.newFixedThreadPool(10)))
                .handle(fileProcessorSendToArtemis(fileIntegrationService, errorFolder))

                .transform(FileProcessingState::getFile)
                .channel(MessageChannels.queue(500))
                .handle(Files.outboundAdapter(new File(archiveFolder)).deleteSourceFiles(true), e -> e.advice(fileRetryAdvice()))

                .get();
    }
    @Bean
    public Advice fileRetryAdvice() {
        return RetryInterceptorBuilder.stateless()
                .maxAttempts(5)
                .backOffOptions(1000, 2.0, 2000)
                .build();
    }


    @Bean
    public GenericHandler<File> fileProcessorInitial(FileIntegrationService fileIntegrationService, String errorFolder) {
        return (file, _) -> {
            Long insertedId = fileIntegrationService.handleAndInitialPersistPrintingData(file);
            if (insertedId == null) {
                fileIntegrationService.handleErrorFile(file, errorFolder);
                throw new IllegalStateException("Failed to process initial file: " + file.getName());
            }
            return new FileProcessingState(file, insertedId);
        };
    }

    @Bean
    public GenericHandler<FileProcessingState> fileProcessorValidate(FileIntegrationService fileIntegrationService, String errorFolder) {
        return (fileProcessingState, _) -> {
            PrintingDataEntity validatedEntity = fileIntegrationService.handleAndValidatePrintingData(fileProcessingState.getInsertedId(), PrintingWayType.ARTEMIS);
            if (validatedEntity == null
                    || (validatedEntity.getPrintingWayType() == PrintingWayType.ARTEMIS
                    && validatedEntity.getPrintingStatus() != PrintingStatus.VALIDATED)) {
                fileIntegrationService.handleErrorFile(fileProcessingState.getFile(), errorFolder);
                throw new IllegalStateException("Failed to validate file: " + fileProcessingState.getFile().getName());
            }
            fileProcessingState.setPrintingDataEntity(validatedEntity);
            return fileProcessingState;
        };
    }

    @Bean
    public GenericHandler<FileProcessingState> fileProcessorSendToArtemis(FileIntegrationService fileIntegrationService, String errorFolder) {
        return (fileProcessingState, _) -> {
            if (fileProcessingState.getPrintingDataEntity().getPrintingWayType() == PrintingWayType.ARTEMIS) {
                if (!fileIntegrationService.handleAndSendPrintingDataToArtemis(fileProcessingState.getInsertedId())) {
                    fileIntegrationService.handleErrorFile(fileProcessingState.getFile(), errorFolder);
                    throw new IllegalStateException("Failed to sent to Artemis file: " + fileProcessingState.getFile().getName());
                }
            }
            return fileProcessingState;
        };
    }
}



