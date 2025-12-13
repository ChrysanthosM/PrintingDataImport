package org.masouras.app.batch.pmp.control.control.validator.domain;

import lombok.Data;

@Data
public class FileValidatorResult {
    public enum ValidationStatus {
        SUCCESS, ERROR
    }

    public static FileValidatorResult success(Object validationResult) { return new FileValidatorResult(ValidationStatus.SUCCESS, null, validationResult); }
    public static FileValidatorResult error(String validationMessage) { return new FileValidatorResult(ValidationStatus.ERROR, validationMessage, null); }

    private final ValidationStatus validationStatus;
    private final String validationMessage;
    private final Object validationResult;
}
