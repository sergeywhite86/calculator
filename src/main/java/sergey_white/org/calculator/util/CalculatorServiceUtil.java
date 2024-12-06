package sergey_white.org.calculator.util;

import lombok.extern.slf4j.Slf4j;
import sergey_white.org.calculator.dto.ScoringDataDto;
import sergey_white.org.calculator.enums.EmploymentStatus;
import sergey_white.org.calculator.enums.Gender;
import sergey_white.org.calculator.exception.RefusalClientException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@Slf4j
public class CalculatorServiceUtil {

    private CalculatorServiceUtil() {
        throw new IllegalStateException("Utility class");
    }

   public static final ArrayList<Predicate<ScoringDataDto>> CONDITIONS_FOR_REFUSAL = new ArrayList<>(List.of(
            user -> {
                log.info("Checking employment status for ScoringDataDto: {}", user);
                if (user.getEmployment().getEmploymentStatus() == EmploymentStatus.UNEMPLOYED) {
                    log.warn("User UNEMPLOYED");
                    throw new RefusalClientException("User UNEMPLOYED");
                }
                return true;
            },
            user -> {
                log.info("Checking amount for ScoringDataDto: {}", user);
                if (user.getAmount().compareTo(user.getEmployment().getSalary().multiply(new BigDecimal("24"))) > 0) {
                    log.warn("User salary*24 < total sum credit");
                    throw new RefusalClientException("User salary*24 < total sum credit");
                }
                return true;
            },
            user -> {
                int age = Period.between(user.getBirthdate(), LocalDate.now()).getYears();
                log.info("Checking age for ScoringDataDto: {}", user);
                if (age < 20 || age > 65) {
                    log.warn("User age < 20 or age > 65");
                    throw new RefusalClientException("User age < 20 and age > 65");
                }
                return true;
            },
            user -> {
                log.info("Checking gender for ScoringDataDto: {}", user);
                if (user.getGender() == Gender.NON_BINARY) {
                    throw new RefusalClientException("User gender is NON_BINARY");
                }
                return true;
            },
            user -> {
                log.info("Checking total work experience for ScoringDataDto: {}", user);
                if (user.getEmployment().getWorkExperienceTotal() < 18) {
                    log.warn("User work experience total < 18");
                    throw new RefusalClientException("User total work experience < 18");
                }
                return true;
            },
            user -> {
                log.info("Checking current work experience for ScoringDataDto: {}", user);
                if (user.getEmployment().getWorkExperienceCurrent() < 3) {
                    log.warn("User work experience current < 3");
                    throw new RefusalClientException("User current work experience < 3");
                }
                return true;
            }
    ));
}

