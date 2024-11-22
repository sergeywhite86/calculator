package sergey_white.org.calculator.domain.loanOfferType;


import sergey_white.org.calculator.dto.LoanOfferDto;
import sergey_white.org.calculator.dto.LoanStatementRequestDto;

import java.math.BigDecimal;
import java.math.RoundingMode;

public interface CalculateLoanOffer {

    LoanOfferDto calculate(LoanStatementRequestDto input);

    BigDecimal calculateRate();

    BigDecimal calculateTotalAmount(LoanStatementRequestDto input);

    default BigDecimal calculateMonthlyPayment(LoanStatementRequestDto input) {
        BigDecimal monthlyRate = calculateRate().divide(new BigDecimal("1200"), 10, RoundingMode.HALF_UP);
        BigDecimal numerator = input.getAmount().multiply(monthlyRate).multiply(BigDecimal.ONE.add(monthlyRate).pow(input.getTerm()));
        BigDecimal denominator = BigDecimal.ONE.add(monthlyRate).pow(input.getTerm()).subtract(BigDecimal.ONE);
        return numerator.divide(denominator, 10, RoundingMode.HALF_UP).setScale(2, RoundingMode.HALF_UP);
    }
}
