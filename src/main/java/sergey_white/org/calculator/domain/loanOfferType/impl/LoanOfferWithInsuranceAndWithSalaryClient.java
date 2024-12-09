package sergey_white.org.calculator.domain.loanOfferType.impl;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import sergey_white.org.calculator.domain.loanOfferType.CalculateLoanOffer;
import sergey_white.org.calculator.dto.LoanOfferDto;
import sergey_white.org.calculator.dto.LoanStatementRequestDto;

import java.math.BigDecimal;


@Component
public class LoanOfferWithInsuranceAndWithSalaryClient implements CalculateLoanOffer {

    @Value("${calculator.base-rate}")
    private BigDecimal baseRate;
    @Value("${calculator.insurance-cost}")
    private BigDecimal insuranceCost;
    @Value("${calculator.insurance-discount}")
    private BigDecimal insuranceDiscount;
    @Value("${calculator.salary-client-discount}")
    private BigDecimal salaryClientDiscount;

    @Override
    public LoanOfferDto calculate(LoanStatementRequestDto input) {
        LoanOfferDto loanOfferDto = new LoanOfferDto();
 
        loanOfferDto.setRequestedAmount(input.getAmount());
        loanOfferDto.setTotalAmount(calculateTotalAmount(input));
        loanOfferDto.setTerm(input.getTerm());
        loanOfferDto.setMonthlyPayment(calculateMonthlyPayment(input,calculateTotalAmount(input)));
        loanOfferDto.setRate(calculateRate());
        loanOfferDto.setIsInsuranceEnabled(true);
        loanOfferDto.setIsSalaryClient(true);

        return loanOfferDto;
    }
    @Override
    public BigDecimal calculateRate() {
        return baseRate.subtract(salaryClientDiscount).subtract(insuranceDiscount);
    }

    @Override
    public BigDecimal calculateTotalAmount(LoanStatementRequestDto input) {
        return input.getAmount().add(insuranceCost);
    }
}
