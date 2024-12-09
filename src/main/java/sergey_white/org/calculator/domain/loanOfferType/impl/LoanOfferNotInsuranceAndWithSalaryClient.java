package sergey_white.org.calculator.domain.loanOfferType.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import sergey_white.org.calculator.domain.loanOfferType.CalculateLoanOffer;
import sergey_white.org.calculator.dto.LoanOfferDto;
import sergey_white.org.calculator.dto.LoanStatementRequestDto;

import java.math.BigDecimal;


@Component
public class LoanOfferNotInsuranceAndWithSalaryClient implements CalculateLoanOffer {

    @Value("${calculator.base-rate}")
    private BigDecimal baseRate;
    @Value("${calculator.salary-client-discount}")
    private BigDecimal salaryClientDiscount;

    private final LoanOfferDto loanOfferDto = new LoanOfferDto();

    @Override
    public LoanOfferDto calculate(LoanStatementRequestDto input) {
        loanOfferDto.setRequestedAmount(input.getAmount());
        loanOfferDto.setTotalAmount(calculateTotalAmount(input));
        loanOfferDto.setTerm(input.getTerm());
        loanOfferDto.setMonthlyPayment(calculateMonthlyPayment(input,calculateTotalAmount(input)));
        loanOfferDto.setRate(calculateRate());
        loanOfferDto.setIsInsuranceEnabled(false);
        loanOfferDto.setIsSalaryClient(true);
        return loanOfferDto;
    }

    @Override
    public BigDecimal calculateRate() {
        return baseRate.subtract(salaryClientDiscount);
    }

    @Override
    public BigDecimal calculateTotalAmount(LoanStatementRequestDto input) {
        return input.getAmount();
    }
}
