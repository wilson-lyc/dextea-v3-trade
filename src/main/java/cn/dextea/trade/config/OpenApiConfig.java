package cn.dextea.trade.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI dexteaTradeOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("德贤茶庄线上点餐系统交易端后台")
                        .version("v1.0.0"));
    }
}
