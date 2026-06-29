package com.bradesco.vaas.controller;

import com.bradesco.vaas.dto.Dtos.VeiculoResponse;
import com.bradesco.vaas.service.VeiculoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposto como a ferramenta MCP {@code consultar_veiculo}
 * (ver docs/04-catalogo-mcp.md).
 */
@RestController
@Tag(name = "Veículo", description = "Consulta de dados sintéticos do veículo")
public class VeiculoController {

    private final VeiculoService veiculoService;

    public VeiculoController(VeiculoService veiculoService) {
        this.veiculoService = veiculoService;
    }

    @Operation(summary = "Consulta dados do veículo por placa e UF",
            description = "Equivalente sintético a uma consulta RENAVAM/FIPE.")
    @GetMapping("/v1/veiculos/{placa}")
    public VeiculoResponse consultar(
            @PathVariable @Parameter(example = "ABC1D23") String placa,
            @RequestParam @Parameter(example = "SP") String uf) {
        return veiculoService.consultar(placa, uf);
    }
}
