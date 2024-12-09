package sergey_white.org.calculator.dto;

import jakarta.validation.constraints.*;
import lombok.Data;


import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class LoanStatementRequestDto {
    @NotNull
    @DecimalMin(value = "20000")
    private BigDecimal amount;
    @NotNull
    @Min(value = 6)
    private Integer term;
    @NotBlank
    @Size(min = 2, max = 30)
    private String firstName;
    @NotBlank
    @Size(min = 2, max = 30)
    private String lastName;
    @Size(min = 2, max = 30)
    private String middleName;
    @NotBlank
    @Pattern(regexp = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$")
    private String email;
    @NotNull
    @PastOrPresent
    private LocalDate birthdate;
    @NotBlank
    @Pattern(regexp = "^\\d{4}$")
    private String passportSeries;
    @NotBlank
    @Pattern(regexp = "^\\d{6}$")
    private String passportNumber;
}