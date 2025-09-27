package org.masouras.config;

import org.masouras.model.PrioritizedFile;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.file.dsl.Files;
import org.springframework.integration.file.filters.AcceptOnceFileListFilter;
import org.springframework.integration.file.locking.NioFileLocker;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.io.File;
import java.util.concurrent.PriorityBlockingQueue;

@Configuration
@EnableIntegration
@IntegrationComponentScan
public class FileIntegrationConfig {
    private static final String WATCH_FOLDER = "D:/MyDocuments/Programming/Files";


    @Bean
    public PriorityBlockingQueue<PrioritizedFile> fileQueue() {
        return new PriorityBlockingQueue<>();
    }

    @Bean
    public TaskExecutor taskExecutorPriority() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setThreadNamePrefix("file-worker-");
        executor.initialize();
        return executor;
    }

    @Bean
    public IntegrationFlow filePollingFlow(PriorityBlockingQueue<PrioritizedFile> fileQueue) {
        return IntegrationFlow
                .from(Files.inboundAdapter(new File(WATCH_FOLDER))
                                .filter(new AcceptOnceFileListFilter<>())
                                .locker(new NioFileLocker())
                                .preventDuplicates(true),
                        e -> e.poller(Pollers.fixedDelay(5000, 5000)))
                .handle(File.class, (file, headers) -> {
                    int priority = determinePriority(file);
                    fileQueue.offer(new PrioritizedFile(file, priority));
                    return null;
                })
                .get();
    }

    private int determinePriority(File file) {
        if (file.getName().contains("urgent")) return 30;
        if (file.getName().contains("report")) return 20;
        return 10;
    }
}


