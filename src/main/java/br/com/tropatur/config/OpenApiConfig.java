package br.com.tropatur.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI tropaturOpenApi() {
        return new OpenAPI().info(new Info().title("TropaTur — API de pontos turísticos")
                .version("1.0.0")
                .description("Projeto acadêmico de turismo histórico de Itapetininga. CRUD demonstrativo; conteúdo cadastrado exige validação histórica. Sem autenticação, destinado à execução local.")
                .contact(new Contact().name("Grupo 3 — Alessandro, Felipe e Viviane")));
    }
}
