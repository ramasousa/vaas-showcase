package com.bradesco.vaas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * VaaS (Vehicle as a Service) — projeto de exemplo.
 *
 * Implementa de forma sintética os domain services descritos no documento
 * "Arquitetura de Execução & Roadmap de Maturidade": consulta de veículo,
 * cálculo de tributos por UF, simulação de financiamento (CaaS/LaaS),
 * gate de política determinístico, execução de pagamento e agendamento
 * de emplacamento.
 *
 * Nenhum dado aqui é real — placas, valores FIPE e alíquotas são gerados
 * de forma determinística (ver {@code docs/06-sandbox-dados-sinteticos.md}).
 */
@SpringBootApplication
public class VaasApplication {
    public static void main(String[] args) {
        SpringApplication.run(VaasApplication.class, args);
    }
}
