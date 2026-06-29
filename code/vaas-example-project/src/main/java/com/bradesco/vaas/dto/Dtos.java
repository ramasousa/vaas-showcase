package com.bradesco.vaas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

/**
 * DTOs do domínio VaaS. Usamos records para manter o contrato explícito e
 * sem boilerplate — cada um corresponde a um schema do OpenAPI (ver
 * docs/openapi/vaas-openapi.yaml) e a uma ferramenta MCP (ver
 * docs/04-catalogo-mcp.md).
 */
public final class Dtos {

    private Dtos() {}

    // ---- consultar_veiculo ----
    public record VeiculoResponse(
            String placa,
            String uf,
            String modelo,
            BigDecimal valorFipe
    ) {}

    // ---- consultar_regra_tributaria ----
    public record RegraTributariaRequest(
            @NotBlank String uf,
            @NotBlank String tipoVeiculo,     // "combustao" | "eletrico"
            @NotNull @Positive BigDecimal valorFipe,
            boolean zeroKm
    ) {}

    public record RegraTributariaResponse(
            int ipvaPercentual,
            BigDecimal ipvaValor,
            BigDecimal licenciamento,
            BigDecimal taxa,
            BigDecimal totalTributos,
            boolean incentivoEletrico
    ) {}

    // ---- simular_financiamento ----
    public record FinanciamentoRequest(
            @NotNull @Positive BigDecimal valor,
            @NotNull @Positive Integer parcelas
    ) {}

    public record FinanciamentoResponse(
            BigDecimal parcelaMensal,
            int parcelas,
            String taxaAm
    ) {}

    // ---- verificar_gate_politica ----
    public record GatePoliticaRequest(
            @NotNull @Positive BigDecimal valorTotal
    ) {}

    public record GatePoliticaResponse(
            boolean aprovado,
            BigDecimal limite,
            String motivo
    ) {}

    // ---- executar_pagamento ----
    public record PagamentoRequest(
            @NotBlank String tipo,            // "veiculo" | "tributo"
            @NotNull @Positive BigDecimal valor,
            @NotBlank String destinatario
    ) {}

    public record PagamentoResponse(
            String status,
            String protocolo,
            String tipo,
            BigDecimal valor
    ) {}

    // ---- agendar_emplacamento ----
    public record EmplacamentoRequest(
            @NotBlank String uf,
            @NotBlank String placa
    ) {}

    public record EmplacamentoResponse(
            String protocolo,
            String dataVistoria,
            String uf
    ) {}

    // ---- erro padrão ----
    public record ErrorResponse(
            String codigo,
            String mensagem
    ) {}
}
