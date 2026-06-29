package com.bradesco.vaas.service;

import com.bradesco.vaas.dto.Dtos.VeiculoResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Consulta sintética de veículo — substitui, neste exemplo, a integração
 * real com RENAVAM/FIPE. Os dados são determinísticos a partir da placa,
 * para que a mesma placa sempre retorne o mesmo veículo (útil em demos e
 * testes). Ver docs/06-sandbox-dados-sinteticos.md.
 */
@Service
public class VeiculoService {

    private static final List<String> MODELOS = List.of(
            "Volkswagen Polo 2023", "Hyundai HB20 2022", "Fiat Pulse 2023",
            "Chevrolet Onix 2024", "Toyota Corolla 2023", "BYD Dolphin 2026"
    );

    public VeiculoResponse consultar(String placa, String uf) {
        int seed = hash(placa + uf);
        String modelo = MODELOS.get(Math.abs(seed) % MODELOS.size());
        long fipe = 45_000L + (Math.abs(seed) % 95_000L);
        BigDecimal valorFipe = BigDecimal.valueOf((fipe / 100) * 100);
        return new VeiculoResponse(placa, uf, modelo, valorFipe);
    }

    private int hash(String s) {
        int h = 0;
        for (int i = 0; i < s.length(); i++) {
            h = h * 31 + s.charAt(i);
        }
        return h;
    }
}
