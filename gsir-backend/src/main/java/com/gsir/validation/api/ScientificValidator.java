package com.gsir.validation.api;

import com.gsir.validation.model.ValidationResult;

public interface ScientificValidator<T> {
    boolean supports(Class<?> clazz);
    ValidationResult validate(T result);
}
