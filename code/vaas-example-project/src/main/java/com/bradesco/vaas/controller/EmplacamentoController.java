package com.bradesco.vaas.controller;

import com.bradesco.vaas.dto.Dtos.EmplacamentoRequest;
import com.bradesco.vaas.dto.Dtos.EmplacamentoResponse;
import com.bradesco.vaas.service.EmplacamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposto como a ferramenta MCP {@code agendar_emplacamento}.
 */
@RestController
@Tag(name = "Emplacamento", description = "Agendamento de vistoria/emplacamento no DETRAN da UF")
public class EmplacamentoController {

    private final EmplacamentoService emplacamentoService;

    public EmplacamentoController(EmplacamentoService emplacamentoService) {
        this.emplacamentoService = emplacamentoService;
    }

    @Operation(summary = "Agenda vistoria/emplacamento")
    @PostMapping("/v1/emplacamentos")
    public EmplacamentoResponse agendar(@Valid @RequestBody EmplacamentoRequest request) {
        return emplacamentoService.agendar(request);
    }
}
