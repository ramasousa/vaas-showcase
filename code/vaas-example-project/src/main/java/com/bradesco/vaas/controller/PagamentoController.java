package com.bradesco.vaas.controller;

import com.bradesco.vaas.dto.Dtos.PagamentoRequest;
import com.bradesco.vaas.dto.Dtos.PagamentoResponse;
import com.bradesco.vaas.service.PagamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposto como a ferramenta MCP {@code executar_pagamento}.
 * Retorna 403 (via {@link com.bradesco.vaas.exception.GlobalExceptionHandler})
 * se o gate de política não tiver aprovado o valor.
 */
@RestController
@Tag(name = "Pagamento", description = "Execução de pagamento — protegida pelo gate de política")
public class PagamentoController {

    private final PagamentoService pagamentoService;

    public PagamentoController(PagamentoService pagamentoService) {
        this.pagamentoService = pagamentoService;
    }

    @Operation(summary = "Executa o pagamento (veículo ou tributo)",
            description = "Reverifica o gate de política internamente antes de processar.")
    @PostMapping("/v1/pagamentos")
    public PagamentoResponse executar(@Valid @RequestBody PagamentoRequest request) {
        return pagamentoService.executar(request);
    }
}
