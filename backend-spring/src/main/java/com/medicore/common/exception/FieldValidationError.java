package com.medicore.common.exception;

public record FieldValidationError(String field, String message) {
}
