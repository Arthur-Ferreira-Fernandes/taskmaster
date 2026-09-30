package br.ifsp.taskmaster.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI taskmasterOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("TASKMASTER API")
                        .description("API RESTful para gerenciamento de tarefas")
                        .version("1.0.0")
                        .contact(new Contact().name("Equipe TASKMASTER")));
    }
}
