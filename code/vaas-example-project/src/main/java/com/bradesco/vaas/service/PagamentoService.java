package com.bradesco.vaas.service;

import com.bradesco.vaas.dto.Dtos.GatePoliticaRequest;
import com.bradesco.vaas.dto.Dtos.PagamentoRequest;
import com.bradesco.vaas.dto.Dtos.PagamentoResponse;
import com.bradesco.vaas.exception.PagamentoNaoAprovadoException;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Execução de pagamento — note que este serviço DEPENDE do
 * {@link GatePoliticaService} e reverifica a aprovação internamente,
 * mesmo que o chamador (agente/MCP) já tenha chamado o gate antes.
 * Defesa em profundidade: a regra de negócio nunca confia apenas no
 * comportamento esperado do agente.
 */
@Service
public class PagamentoService {

    private final GatePoliticaService gatePoliticaService;

    public PagamentoService(GatePoliticaService gatePoliticaService) {
        this.gatePoliticaService = gatePoliticaService;
    }

    public PagamentoResponse executar(PagamentoRequest req) {
        var gate = gatePoliticaService.verificar(new GatePoliticaRequest(req.valor()));
        if (!gate.aprovado()) {
            throw new PagamentoNaoAprovadoException(
                    "Pagamento de valor " + req.valor() + " bloqueado pelo gate de política: " + gate.motivo());
        }

        String protocolo = "BRD-2026-" + ThreadLocalRandom.current().nextInt(10_000, 99_999);
        return new PagamentoResponse("confirmado", protocolo, req.tipo(), req.valor());
    }
}
