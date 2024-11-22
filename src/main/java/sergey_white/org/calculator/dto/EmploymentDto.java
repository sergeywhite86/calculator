package sergey_white.org.calculator.dto;

import lombok.Data;
import sergey_white.org.calculator.enums.EmploymentStatus;
import sergey_white.org.calculator.enums.Position;

import java.math.BigDecimal;

@Data
public class EmploymentDto {
    private EmploymentStatus employmentStatus;
    private String employerINN;
    private BigDecimal salary;
    private Position position;
    private Integer workExperienceTotal;
    private Integer workExperienceCurrent;
}
