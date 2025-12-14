package org.masouras.app.batch.pmp.domain;

import lombok.Data;

@Data
public class FileValidatorResult {
    public enum ValidationStatus {
        SUCCESS, ERROR
    }

    public static FileValidatorResult success(Object result) { return new FileValidatorResult(ValidationStatus.SUCCESS, null, result); }
    public static FileValidatorResult error(String message) { return new FileValidatorResult(ValidationStatus.ERROR, message, null); }

    private final ValidationStatus status;
    private final String message;
    private final Object result;
}
