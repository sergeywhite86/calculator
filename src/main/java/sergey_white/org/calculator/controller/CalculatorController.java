package sergey_white.org.calculator.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sergey_white.org.calculator.dto.CreditDto;
import sergey_white.org.calculator.dto.LoanOfferDto;
import sergey_white.org.calculator.dto.LoanStatementRequestDto;
import sergey_white.org.calculator.dto.ScoringDataDto;
import sergey_white.org.calculator.service.CalculatorService;

import java.util.List;

@RestController
@RequestMapping("/calculator")
@RequiredArgsConstructor
public class CalculatorController {

    private final CalculatorService service;

    @Operation(summary = "Расчёт возможных условий кредита",
            description = """
    Рассчитать предварительно и вернуть список из 4 предложений по кредитам.При расчете будет учтено
    берет ли клиент страховку и является ли клиент зарплатным для банка
    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешная операция",
                    content = @Content(schema = @Schema(implementation = LoanOfferDto.class))),
            @ApiResponse(responseCode = "400", description = "Неверный ввод")
    })
    @PostMapping("/offers")
    public List<LoanOfferDto> calculateLoanOffers(@RequestBody LoanStatementRequestDto req) {
        return service.getLoanOffers(req);
    }

    @Operation(summary = "Расчет кредита", description = """
             Рассчитать и вернуть детали кредита на основе
             предоставленных данных скоринга.
             """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешная операция",
                    content = @Content(schema = @Schema(implementation = CreditDto.class))),
            @ApiResponse(responseCode = "400", description = "Неверный ввод")
    })
    @PostMapping("/calc")
    public CreditDto calculateCredit(@RequestBody ScoringDataDto req) {
        return service.calculateCredit(req);
    }
}