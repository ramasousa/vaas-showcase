package com.bradesco.vaas.controller;

import com.bradesco.vaas.dto.Dtos.FinanciamentoRequest;
import com.bradesco.vaas.dto.Dtos.FinanciamentoResponse;
import com.bradesco.vaas.service.CreditoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposto como a ferramenta MCP {@code simular_financiamento}.
 * Neste exemplo fica no domínio VaaS só para demonstração end-to-end —
 * em produção, consome os squads de CaaS/LaaS já existentes (não duplicar).
 */
@RestController
@Tag(name = "Crédito", description = "Simulação de financiamento (CaaS/LaaS)")
public class CreditoController {

    private final CreditoService creditoService;

    public CreditoController(CreditoService creditoService) {
        this.creditoService = creditoService;
    }

    @Operation(summary = "Simula parcela de financiamento")
    @PostMapping("/v1/credito/simular")
    public FinanciamentoResponse simular(@Valid @RequestBody FinanciamentoRequest request) {
        return creditoService.simular(request);
    }
}
