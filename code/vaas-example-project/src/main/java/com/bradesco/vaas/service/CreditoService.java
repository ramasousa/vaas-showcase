package com.bradesco.vaas.service;

import com.bradesco.vaas.dto.Dtos.FinanciamentoRequest;
import com.bradesco.vaas.dto.Dtos.FinanciamentoResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Simulação de financiamento — neste exemplo, fica no domínio VaaS só pra
 * fins de demonstração end-to-end. Em produção, a VaaS NÃO recalcula crédito:
 * ela consome os squads de CaaS (embedded, ticket menor) ou LaaS
 * (estruturado, ticket maior) já existentes na Tribe BaaS — ver seção
 * "Squads já em andamento a não duplicar" do documento de arquitetura.
 */
@Service
public class CreditoService {

    private static final BigDecimal TAXA_AM = BigDecimal.valueOf(1.19);
    private static final BigDecimal FATOR_ENCARGO = BigDecimal.valueOf(1.15);

    public FinanciamentoResponse simular(FinanciamentoRequest req) {
        BigDecimal parcela = req.valor()
                .multiply(FATOR_ENCARGO)
                .divide(BigDecimal.valueOf(req.parcelas()), 2, RoundingMode.HALF_UP);

        return new FinanciamentoResponse(parcela, req.parcelas(), TAXA_AM + "%");
    }
}
