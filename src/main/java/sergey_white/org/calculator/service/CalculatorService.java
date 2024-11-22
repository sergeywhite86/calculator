package sergey_white.org.calculator.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sergey_white.org.calculator.domain.loanOfferType.CalculateLoanOffer;
import sergey_white.org.calculator.dto.*;
import sergey_white.org.calculator.enums.EmploymentStatus;
import sergey_white.org.calculator.enums.Gender;
import sergey_white.org.calculator.enums.MaritalStatus;
import sergey_white.org.calculator.enums.Position;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.function.Predicate;

@Service
@RequiredArgsConstructor
@Slf4j
public class CalculatorService {
    private final ValidatorService validatorService;

    @Value("${calculator.base-rate}")
    private BigDecimal baseRate;
    @Value("${calculator.insurance-cost}")
    private BigDecimal insuranceCost;
    @Value("${calculator.insurance-discount}")
    private BigDecimal insuranceDiscount;
    @Value("${calculator.salary-client-discount}")
    private BigDecimal salaryClientDiscount;

    private final List<CalculateLoanOffer> offers;

    public List<LoanOfferDto> getLoanOffers(LoanStatementRequestDto input) {
        if (validatorService.validate(input)) {
            log.warn("Validation failed for LoanStatementRequestDto: {}", input);
            return Collections.emptyList();
        }
        log.info("Calculating loan offers for LoanStatementRequestDto: {}", input);
        return offers.stream()
                .map(offer -> offer.calculate(input))
                .sorted(Comparator.comparing(LoanOfferDto::getRate).reversed())
                .toList();
    }

    public CreditDto calculateCredit(ScoringDataDto input) {
        if (validatorService.validate(input)) {
            log.warn("Validation failed for ScoringDataDto: {}", input);
            return null;
        }
        log.info("Calculating credit for ScoringDataDto: {}", input);
        CreditDto creditDto = new CreditDto();
        for (Predicate<ScoringDataDto> condition : conditionsForRefusal) {
            if (condition.test(input)) {
                log.info("Refusal condition met for ScoringDataDto: {}", input);
                return null;
            }
            creditDto.setAmount(calculateAmount(input));
            creditDto.setTerm(input.getTerm());
            creditDto.setMonthlyPayment(calculateMonthlyPayment(input));
            creditDto.setRate(calculateRate(input));
            creditDto.setPsk(calculatePSK(input));
            creditDto.setIsInsuranceEnabled(input.getIsInsuranceEnabled());
            creditDto.setIsSalaryClient(input.getIsSalaryClient());
            creditDto.setPaymentSchedule(calculatePaymentSchedule(input));
        }

        log.info("Credit calculation completed for ScoringDataDto: {}", input);
        return creditDto;
    }

    private BigDecimal calculatePSK(ScoringDataDto input) {
        log.info("Calculating PSK for ScoringDataDto: {}", input);
        return calculateMonthlyPayment(input).multiply(BigDecimal.valueOf(input.getTerm()));
    }

    private BigDecimal calculateMonthlyPayment(ScoringDataDto input) {
        log.info("Calculating monthly payment for ScoringDataDto: {}", input);
        BigDecimal monthlyRate = calculateRate(input).divide(new BigDecimal("1200"), 10, RoundingMode.HALF_UP);
        BigDecimal numerator = calculateAmount(input).multiply(monthlyRate).multiply(BigDecimal.ONE.add(monthlyRate).pow(input.getTerm()));
        BigDecimal denominator = BigDecimal.ONE.add(monthlyRate).pow(input.getTerm()).subtract(BigDecimal.ONE);
        return numerator.divide(denominator, 10, RoundingMode.HALF_UP).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateAmount(ScoringDataDto input) {
        log.info("Calculating amount for ScoringDataDto: {}", input);
        return input.getIsInsuranceEnabled() ? insuranceCost.add(input.getAmount()) : input.getAmount();
    }

    private BigDecimal calculateRate(ScoringDataDto input) {
        log.info("Calculating rate for ScoringDataDto: {}", input);
        BigDecimal rate = baseRate;

        if (input.getEmployment().getEmploymentStatus() == EmploymentStatus.SELF_EMPLOYED) {
            rate = rate.add(new BigDecimal("2.0"));
        } else if (input.getEmployment().getEmploymentStatus() == EmploymentStatus.BUSINESS_OWNER) {
            rate = rate.add(new BigDecimal("1.0"));
        }

        if (input.getEmployment().getPosition() == Position.MID_MANAGER) {
            rate = rate.subtract(new BigDecimal("2.0"));
        } else if (input.getEmployment().getPosition() == Position.TOP_MANAGER) {
            rate = rate.subtract(new BigDecimal("3.0"));
        }

        if (input.getMaritalStatus() == MaritalStatus.MARRIED) {
            rate = rate.subtract(new BigDecimal("3.0"));
        } else if (input.getMaritalStatus() == MaritalStatus.DIVORCED) {
            rate = rate.add(new BigDecimal("1.0"));
        }

        int age = Period.between(input.getBirthdate(), LocalDate.now()).getYears();
        if (input.getGender() == Gender.FEMALE && age >= 32 && age <= 60) {
            rate = rate.subtract(new BigDecimal("3.0"));
        } else if (input.getGender() == Gender.MALE && age >= 30 && age <= 55) {
            rate = rate.subtract(new BigDecimal("3.0"));
        }

        if (input.getIsInsuranceEnabled()) {
            rate = rate.subtract(new BigDecimal(String.valueOf(insuranceDiscount)));
        }

        if (input.getIsSalaryClient()) {
            rate = rate.subtract(new BigDecimal(String.valueOf(salaryClientDiscount)));
        }
        return rate;
    }

    private List<PaymentScheduleElementDto> calculatePaymentSchedule(ScoringDataDto input) {
        log.info("Calculating payment schedule for ScoringDataDto: {}", input);
        List<PaymentScheduleElementDto> paymentSchedule = new ArrayList<>();
        BigDecimal remainingDebt = calculateAmount(input);
        BigDecimal monthlyRate = calculateRate(input).divide(new BigDecimal("1200"), 10, RoundingMode.HALF_UP);
        LocalDate currentDate = LocalDate.now().plusMonths(1);

        for (int i = 1; i <= input.getTerm(); i++) {
            PaymentScheduleElementDto payment = new PaymentScheduleElementDto();
            payment.setNumber(i);
            payment.setDate(currentDate);

            BigDecimal interestPayment = remainingDebt.multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP);
            BigDecimal debtPayment = calculateMonthlyPayment(input).subtract(interestPayment)
                    .setScale(2, RoundingMode.HALF_UP);
            remainingDebt = remainingDebt.subtract(debtPayment).setScale(2, RoundingMode.HALF_UP);

            payment.setTotalPayment(calculateMonthlyPayment(input));
            payment.setInterestPayment(interestPayment);
            payment.setDebtPayment(debtPayment);
            payment.setRemainingDebt(remainingDebt);

            paymentSchedule.add(payment);
            currentDate = currentDate.plusMonths(1);
        }

        log.info("Payment schedule calculation completed for ScoringDataDto: {}", input);
        return paymentSchedule;
    }

    private final ArrayList<Predicate<ScoringDataDto>> conditionsForRefusal = new ArrayList<>(Arrays.asList(
            user -> {
                log.info("Checking employment status for ScoringDataDto: {}", user);
                return user.getEmployment().getEmploymentStatus() == EmploymentStatus.UNEMPLOYED;
            },
            user -> {
                log.info("Checking amount for ScoringDataDto: {}", user);
                return user.getAmount().compareTo(user.getEmployment().getSalary().multiply(new BigDecimal("24"))) > 0;
            },
            user -> {
                int age = Period.between(user.getBirthdate(), LocalDate.now()).getYears();
                log.info("Checking age for ScoringDataDto: {}", user);
                return age < 20 || age > 65;
            },
            user -> {
                log.info("Checking gender for ScoringDataDto: {}", user);
                return user.getGender() == Gender.NON_BINARY;
            },
            user -> {
                log.info("Checking total work experience for ScoringDataDto: {}", user);
                return user.getEmployment().getWorkExperienceTotal() < 18;
            },
            user -> {
                log.info("Checking current work experience for ScoringDataDto: {}", user);
                return user.getEmployment().getWorkExperienceCurrent() < 3;
            }
    ));
}