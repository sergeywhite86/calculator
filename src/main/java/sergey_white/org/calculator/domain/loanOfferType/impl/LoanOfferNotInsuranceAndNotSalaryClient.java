package sergey_white.org.calculator.domain.loanOfferType.impl;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import sergey_white.org.calculator.domain.loanOfferType.CalculateLoanOffer;
import sergey_white.org.calculator.dto.LoanOfferDto;
import sergey_white.org.calculator.dto.LoanStatementRequestDto;

import java.math.BigDecimal;


@Component
public class LoanOfferNotInsuranceAndNotSalaryClient implements CalculateLoanOffer {

    @Value("${calculator.base-rate}")
    private BigDecimal baseRate;

    private final LoanOfferDto loanOfferDto = new LoanOfferDto();
    @Override
    public LoanOfferDto calculate(LoanStatementRequestDto input) {
        loanOfferDto.setRequestedAmount(input.getAmount());
        loanOfferDto.setTotalAmount(calculateTotalAmount(input));
        loanOfferDto.setTerm(input.getTerm());
        loanOfferDto.setMonthlyPayment(calculateMonthlyPayment(input));
        loanOfferDto.setRate(calculateRate());
        loanOfferDto.setIsInsuranceEnabled(false);
        loanOfferDto.setIsSalaryClient(false);
        return loanOfferDto;
    }

    @Override
    public BigDecimal calculateRate() {
        return baseRate;
    }

    @Override
    public BigDecimal calculateTotalAmount(LoanStatementRequestDto input) {
        return input.getAmount();
    }
}

