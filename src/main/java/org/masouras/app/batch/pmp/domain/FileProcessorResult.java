package org.masouras.app.batch.pmp.domain;

import lombok.Data;

@Data
public class FileProcessorResult {
    public enum ProcessorStatus {
        SUCCESS, ERROR
    }

    public static FileProcessorResult success(Object result) { return new FileProcessorResult(ProcessorStatus.SUCCESS, null, result); }
    public static FileProcessorResult error(String message) { return new FileProcessorResult(ProcessorStatus.ERROR, message, null); }

    private final ProcessorStatus status;
    private final String message;
    private final Object result;
}
