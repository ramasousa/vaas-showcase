package com.bradesco.vaas;

import com.bradesco.vaas.dto.Dtos.PagamentoRequest;
import com.bradesco.vaas.exception.PagamentoNaoAprovadoException;
import com.bradesco.vaas.service.GatePoliticaService;
import com.bradesco.vaas.service.PagamentoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * O teste mais importante deste projeto: garante que o gate de política
 * é uma barreira de verdade, não decorativa — nenhum valor acima do
 * limite configurado deve conseguir ser pago, mesmo chamando o serviço
 * diretamente (sem passar pelo agente).
 */
@SpringBootTest
class GatePoliticaServiceTest {

    @Autowired
    private GatePoliticaService gatePoliticaService;

    @Autowired
    private PagamentoService pagamentoService;

    @Test
    void deveAprovarValorDentroDoLimite() {
        ReflectionTestUtils.setField(gatePoliticaService, "limiteAprovacaoAutomatica", BigDecimal.valueOf(80000));
        var resultado = gatePoliticaService.verificar(new com.bradesco.vaas.dto.Dtos.GatePoliticaRequest(BigDecimal.valueOf(50000)));
        assertThat(resultado.aprovado()).isTrue();
    }

    @Test
    void deveBloquearPagamentoAcimaDoLimiteMesmoSemChamarOGateAntes() {
        var request = new PagamentoRequest("veiculo", BigDecimal.valueOf(150_000), "vendedor-exemplo");

        assertThatThrownBy(() -> pagamentoService.executar(request))
                .isInstanceOf(PagamentoNaoAprovadoException.class)
                .hasMessageContaining("bloqueado pelo gate de política");
    }
}
