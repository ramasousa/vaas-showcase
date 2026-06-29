package com.bradesco.vaas.controller;

import com.bradesco.vaas.dto.Dtos.RegraTributariaRequest;
import com.bradesco.vaas.dto.Dtos.RegraTributariaResponse;
import com.bradesco.vaas.service.TributosService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposto como a ferramenta MCP {@code consultar_regra_tributaria}.
 * Em produção, este endpoint consulta o Bedrock Knowledge Base (RAG) em vez
 * da tabela em memória — ver ADR-003.
 */
@RestController
@Tag(name = "Tributos", description = "Cálculo de IPVA, licenciamento e taxa por UF")
public class TributosController {

    private final TributosService tributosService;

    public TributosController(TributosService tributosService) {
        this.tributosService = tributosService;
    }

    @Operation(summary = "Calcula tributos de um veículo para uma UF",
            description = "Considera incentivo de IPVA para veículos elétricos.")
    @PostMapping("/v1/tributos/calcular")
    public RegraTributariaResponse calcular(@Valid @RequestBody RegraTributariaRequest request) {
        return tributosService.calcular(request);
    }
}
