package com.bradesco.vaas.service;

import com.bradesco.vaas.dto.Dtos.RegraTributariaRequest;
import com.bradesco.vaas.dto.Dtos.RegraTributariaResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * Cálculo de tributos por UF — em produção, isso é o que vira uma consulta
 * ao Bedrock Knowledge Base (RAG) sobre a base de regras tributárias por
 * estado (ver ADR-003). Aqui, simulamos com uma tabela fixa em memória,
 * exatamente como nos protótipos navegáveis.
 *
 * Alíquotas SINTÉTICAS — não usar como referência fiscal real.
 */
@Service
public class TributosService {

    private static final Map<String, Integer> ALIQUOTA_IPVA_PADRAO = Map.of(
            "SP", 4, "RJ", 4, "MG", 3
    );
    private static final int ALIQUOTA_IPVA_DEFAULT = 4;

    public RegraTributariaResponse calcular(RegraTributariaRequest req) {
        int aliquota = ALIQUOTA_IPVA_PADRAO.getOrDefault(req.uf(), ALIQUOTA_IPVA_DEFAULT);
        boolean eletrico = "eletrico".equalsIgnoreCase(req.tipoVeiculo());
        if (eletrico) {
            // Incentivo a veículos elétricos — vários estados já praticam alíquota
            // reduzida; aqui simplificamos como 1/3 da alíquota padrão, mínimo 1%.
            aliquota = Math.max(1, aliquota / 3);
        }

        BigDecimal ipvaValor = req.valorFipe()
                .multiply(BigDecimal.valueOf(aliquota))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        BigDecimal licenciamento = BigDecimal.valueOf(150 + (Math.abs(hash(req.uf())) % 100));
        BigDecimal taxa = req.zeroKm()
                ? BigDecimal.valueOf(200 + (Math.abs(hash(req.uf() + "0km")) % 100))
                : BigDecimal.valueOf(120 + (Math.abs(hash(req.uf() + "usado")) % 150));

        BigDecimal total = ipvaValor.add(licenciamento).add(taxa);

        return new RegraTributariaResponse(aliquota, ipvaValor, licenciamento, taxa, total, eletrico);
    }

    private int hash(String s) {
        int h = 0;
        for (int i = 0; i < s.length(); i++) h = h * 31 + s.charAt(i);
        return h;
    }
}
