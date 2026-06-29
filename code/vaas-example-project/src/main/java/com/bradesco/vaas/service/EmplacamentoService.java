package com.bradesco.vaas.service;

import com.bradesco.vaas.dto.Dtos.EmplacamentoRequest;
import com.bradesco.vaas.dto.Dtos.EmplacamentoResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Agendamento de emplacamento/vistoria — em produção, este serviço chama o
 * conector real de integração com o DETRAN da UF (um por estado, com
 * maturidade de API desigual — ver "Riscos & Latência" no showcase original).
 * Aqui, simulamos uma resposta determinística com pequena variação por UF.
 */
@Service
public class EmplacamentoService {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM");

    public EmplacamentoResponse agendar(EmplacamentoRequest req) {
        String protocolo = "DETRAN-" + req.uf() + "-" + ThreadLocalRandom.current().nextInt(10_000, 99_999);
        String dataVistoria = LocalDate.now().plusDays(5 + ThreadLocalRandom.current().nextInt(10)).format(FORMATO);
        return new EmplacamentoResponse(protocolo, dataVistoria, req.uf());
    }
}
