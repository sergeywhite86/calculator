package sergey_white.org.calculator.service;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sergey_white.org.calculator.exception.ValidationException;

import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor()
public class ValidatorService {
    private final Validator validator;

    public <T> void validate(T object) {
        Set<ConstraintViolation<T>> exceptions = validator.validate(object);
        if (!exceptions.isEmpty()) {
            for (ConstraintViolation<T> exception : exceptions) {
                log.warn("Validation failed: {} - {}", exception.getPropertyPath(), exception.getMessage());
                throw new ValidationException(String.format("Validation error: %s %s",
                        exception.getPropertyPath(), exception.getMessage()));
            }
        }
    }
}




