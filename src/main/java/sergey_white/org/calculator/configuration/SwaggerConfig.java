package sergey_white.org.calculator.configuration;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI calculatorOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Calculator API")
                        .version("1.0")
                        .description("API для расчета предложений по кредиту и деталей кредита"));
    }
}