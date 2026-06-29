package com.bradesco.vaas.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração do OpenAPI exposto em /v3/api-docs e /swagger-ui.html.
 * O esquema de segurança aqui é ILUSTRATIVO (API Key simples) — em
 * produção, a autenticação real acontece no BFF/Apigee via OAuth2 Token
 * Exchange, conforme o documento "Fluxo de Requisição: MFE → Core".
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI vaasOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("VaaS API — Vehicle as a Service")
                        .version("0.1.0")
                        .description("""
                                APIs de domínio do VaaS: consulta de veículo, cálculo de tributos,
                                simulação de financiamento, gate de política, execução de pagamento
                                e agendamento de emplacamento.

                                Todos os dados retornados são SINTÉTICOS — ver
                                docs/06-sandbox-dados-sinteticos.md.

                                Pensado para ser exposto via Apigee como MCP proxy (ADR-001),
                                tornando cada operação abaixo disponível como ferramenta para
                                agentes de IA sem nenhuma reescrita de código.
                                """)
                        .contact(new Contact()
                                .name("Open Platform — APIs, BaaS & Embedded Finance")
                                .email("open-platform@bradesco.com.br")))
                .components(new Components()
                        .addSecuritySchemes("ApiKeyAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.HEADER)
                                        .name("X-API-Key")));
    }
}
