package com.bradesco.vaas.service;

import com.bradesco.vaas.dto.Dtos.GatePoliticaRequest;
import com.bradesco.vaas.dto.Dtos.GatePoliticaResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Gate de Política — o coração da governança do Modelo B (ver ADR-004).
 *
 * Esta é a peça mais importante deste projeto de exemplo: a aprovação
 * automática é uma REGRA FIXA, configurada via application.yml, e NUNCA
 * uma decisão do agente/modelo de IA. {@link PagamentoService} depende
 * desta classe e bloqueia qualquer execução que não tenha sido aprovada
 * aqui primeiro — ver o fluxo completo em
 * docs/02-autenticacao.md e no documento "Fluxo de Requisição: MFE → Core".
 */
@Service
public class GatePoliticaService {

    @Value("${vaas.gate.limite-aprovacao-automatica:80000}")
    private BigDecimal limiteAprovacaoAutomatica;

    public GatePoliticaResponse verificar(GatePoliticaRequest req) {
        boolean aprovado = req.valorTotal().compareTo(limiteAprovacaoAutomatica) <= 0;
        String motivo = aprovado
                ? "dentro do limite de auto-aprovação"
                : "acima do limite — requer validação manual";
        return new GatePoliticaResponse(aprovado, limiteAprovacaoAutomatica, motivo);
    }
}
