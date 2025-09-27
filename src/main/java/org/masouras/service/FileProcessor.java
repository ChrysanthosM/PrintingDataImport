package org.masouras.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.masouras.model.PrioritizedFile;
import org.masouras.strategy.FileProcessorBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class FileProcessor {
    private final PriorityBlockingQueue<PrioritizedFile> fileQueue;
    private final ScheduledExecutorService delayScheduler = Executors.newSingleThreadScheduledExecutor();

    private final List<FileProcessorBase> fileProcessorBaseList;
    private final TaskExecutor taskExecutorPriority;

    @Autowired
    public FileProcessor(PriorityBlockingQueue<PrioritizedFile> fileQueue, List<FileProcessorBase> fileProcessorBaseList,
                         @Qualifier("taskExecutorPriority") TaskExecutor taskExecutorPriority) {
        this.fileQueue = fileQueue;
        this.fileProcessorBaseList = fileProcessorBaseList;
        this.taskExecutorPriority = taskExecutorPriority;
    }

    @PostConstruct
    public void startProcessing() {
        taskExecutorPriority.execute(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    PrioritizedFile pf = fileQueue.take();
                    if (!processFile(pf.getFile())) {
                        delayScheduler.schedule(() -> fileQueue.offer(pf), 1, TimeUnit.SECONDS);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }
    @PreDestroy
    public void shutdownExecutor() {
        if (taskExecutorPriority instanceof ThreadPoolTaskExecutor executor) {
            executor.shutdown();
        }
    }

    private boolean processFile(File file) {
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw");
             FileChannel channel = raf.getChannel();
             FileLock lock = channel.tryLock()) {
            if (lock == null) {
                log.warn("File is currently locked and cannot be processed: {}", file.getAbsolutePath());
                return false;
            }

            // Proceed with processing
            String extension = getExtension(file.getName());
            fileProcessorBaseList.stream()
                    .filter(p -> p.getSupportedExtensionType().getExtension().equalsIgnoreCase(extension))
                    .findFirst()
                    .ifPresentOrElse(
                            p -> p.process(file),
                            () -> log.warn("No processor found for file type: {}", extension)
                    );
            return true;
        } catch (IOException e) {
            log.error("Error checking lock or processing file: {}", file.getAbsolutePath(), e);
            return false;
        }
    }

    private String getExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex != -1) ? filename.substring(dotIndex + 1) : StringUtils.EMPTY;
    }
}



