package org.ulpgc.business.service;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

public class EntityValidator {
    private static final ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
    private static final Validator validator = validatorFactory.getValidator();

    /**
     * Validates the given entity using Bean Validation
     * @param entity The entity to validate
     * @param <T> Type of the entity
     * @throws ValidationException if validation fails
     */
    public <T> void validate(T entity) {
        Set<ConstraintViolation<T>> violations = validator.validate(entity);

        if (!violations.isEmpty()) {
            StringBuilder errorMessage = new StringBuilder("Validation failed:");
            for (ConstraintViolation<T> violation : violations) {
                errorMessage.append("\n - ")
                        .append(violation.getPropertyPath())
                        .append(": ")
                        .append(violation.getMessage());
            }
            throw new ValidationException(errorMessage.toString());
        }
    }

    /**
     * Custom exception for validation errors
     */
    public static class ValidationException extends RuntimeException {
        public ValidationException(String message) {
            super(message);
        }
    }
}