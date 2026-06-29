package com.bradesco.vaas.controller;

import com.bradesco.vaas.dto.Dtos.GatePoliticaRequest;
import com.bradesco.vaas.dto.Dtos.GatePoliticaResponse;
import com.bradesco.vaas.service.GatePoliticaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposto como a ferramenta MCP {@code verificar_gate_politica}.
 *
 * O system prompt do agente instrui o modelo a SEMPRE chamar esta
 * ferramenta antes de {@code executar_pagamento} — mas, como reforço,
 * {@link com.bradesco.vaas.service.PagamentoService} também a chama
 * internamente. Esta é a única ferramenta do catálogo que nunca deveria
 * ser substituída por uma decisão de IA.
 */
@RestController
@Tag(name = "Gate de Política", description = "Aprovação determinística de valores — nunca decisão do modelo")
public class GatePoliticaController {

    private final GatePoliticaService gatePoliticaService;

    public GatePoliticaController(GatePoliticaService gatePoliticaService) {
        this.gatePoliticaService = gatePoliticaService;
    }

    @Operation(summary = "Verifica se um valor pode ser aprovado automaticamente",
            description = "Regra fixa, configurável via application.yml (vaas.gate.limite-aprovacao-automatica).")
    @PostMapping("/v1/gate/politica")
    public GatePoliticaResponse verificar(@Valid @RequestBody GatePoliticaRequest request) {
        return gatePoliticaService.verificar(request);
    }
}
