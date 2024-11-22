package sergey_white.org.calculator.service;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ValidatorService {
    private final Validator validator;

    public <T> boolean validate(T object) {
        Set<ConstraintViolation<T>> exceptions = validator.validate(object);
        if (!exceptions.isEmpty()) {
            for (ConstraintViolation<T> exception : exceptions) {
                log.warn("Validation failed: {} - {}", exception.getPropertyPath(), exception.getMessage());
            }
            return true;
        }
        return false;
    }
}
