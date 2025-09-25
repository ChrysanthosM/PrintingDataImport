package org.masouras.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.masouras.model.PrioritizedFile;
import org.masouras.strategy.FileProcessorBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;

@Component
@Slf4j
public class FileProcessor {
    private final TaskExecutor taskExecutor;
    private final PriorityBlockingQueue<PrioritizedFile> fileQueue;
    private final List<FileProcessorBase> fileProcessorBaseList;

    @Autowired
    public FileProcessor(TaskExecutor taskExecutor, PriorityBlockingQueue<PrioritizedFile> fileQueue, List<FileProcessorBase> fileProcessorBaseList) {
        this.taskExecutor = taskExecutor;
        this.fileQueue = fileQueue;
        this.fileProcessorBaseList = fileProcessorBaseList;
    }

    @PostConstruct
    public void startProcessing() {
        taskExecutor.execute(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    PrioritizedFile pf = fileQueue.take();
                    taskExecutor.execute(() -> processFile(pf.getFile()));
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
    }
    @PreDestroy
    public void shutdownExecutor() {
        if (taskExecutor instanceof ThreadPoolTaskExecutor executor) {
            executor.shutdown();
        }
    }

    private void processFile(File file) {
        String extension = getExtension(file.getName());
        fileProcessorBaseList.stream()
                .filter(p -> p.getSupportedExtensionType().getExtension().equalsIgnoreCase(extension))
                .findFirst()
                .ifPresentOrElse(
                        p -> p.process(file),
                        () -> log.warn("No processor found for file type: {}", extension)
                );
    }
    private String getExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex != -1) ? filename.substring(dotIndex + 1) : StringUtils.EMPTY;
    }
}



