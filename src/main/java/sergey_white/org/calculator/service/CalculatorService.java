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


import static sergey_white.org.calculator.util.CalculatorServiceUtil.CONDITIONS_FOR_REFUSAL;

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
        validatorService.validate(input);
        log.info("Calculating loan offers for LoanStatementRequestDto: {}", input);
        return offers.stream()
                .map(offer -> offer.calculate(input))
                .sorted(Comparator.comparing(LoanOfferDto::getRate).reversed())
                .toList();
    }

    public CreditDto calculateCredit(ScoringDataDto input) {
        validatorService.validate(input);
        log.info("Check for refusal credit : {}", input);
        checkUserForRefusal(input);

        log.info("Calculating credit for ScoringDataDto: {}", input);
        CreditDto creditDto = new CreditDto();
        creditDto.setAmount(calculateAmount(input));
        creditDto.setTerm(input.getTerm());
        creditDto.setMonthlyPayment(calculateMonthlyPayment(input));
        creditDto.setRate(calculateRate(input));
        creditDto.setPsk(calculatePSK(input));
        creditDto.setIsInsuranceEnabled(input.getIsInsuranceEnabled());
        creditDto.setIsSalaryClient(input.getIsSalaryClient());
        creditDto.setPaymentSchedule(calculatePaymentSchedule(input));
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

    private void checkUserForRefusal(ScoringDataDto input) {
        CONDITIONS_FOR_REFUSAL
                .forEach(condition -> condition.test(input));
    }
}

