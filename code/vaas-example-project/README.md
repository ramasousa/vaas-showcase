# VaaS — Vehicle as a Service (projeto de exemplo)

Implementação de referência dos domain services descritos no documento
**"Arquitetura de Execução & Roadmap de Maturidade"** (Open Platform — APIs,
BaaS & Embedded Finance). Java 21 + Spring Boot 3.3.

> ⚠️ Todos os dados retornados são **sintéticos** — placas, valores FIPE e
> alíquotas tributárias são gerados de forma determinística, não vêm de
> nenhuma fonte oficial. Ver `../../docs/developer-portal/06-sandbox-dados-sinteticos.md` (no showcase completo).

## Como rodar

```bash
mvn spring-boot:run
```

A API sobe em `http://localhost:8080`. Documentação interativa:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

Todas as chamadas (exceto documentação) exigem o header `X-API-Key: demo-key-troque-em-producao`
— ver `../../docs/developer-portal/02-autenticacao.md` (no showcase completo) para o motivo de isso ser só um placeholder.

## O que está implementado

| Endpoint | Ferramenta MCP equivalente | Domain service |
|---|---|---|
| `GET /v1/veiculos/{placa}?uf=SP` | `consultar_veiculo` | `VeiculoService` |
| `POST /v1/tributos/calcular` | `consultar_regra_tributaria` | `TributosService` |
| `POST /v1/credito/simular` | `simular_financiamento` | `CreditoService` |
| `POST /v1/gate/politica` | `verificar_gate_politica` | `GatePoliticaService` |
| `POST /v1/pagamentos` | `executar_pagamento` | `PagamentoService` |
| `POST /v1/emplacamentos` | `agendar_emplacamento` | `EmplacamentoService` |

Essa é a mesma tabela de ferramentas usada nos protótipos navegáveis (chat +
orquestração) e no backend multi-provedor (Claude/GPT/Gemini) — a lógica
sintética foi mantida idêntica de propósito, pra rastreabilidade entre tudo
que já foi construído.

## A peça que mais importa: o Gate de Política

`PagamentoService` **nunca** confia que o chamador já verificou o gate antes
— ele chama `GatePoliticaService` internamente e lança
`PagamentoNaoAprovadoException` (→ HTTP 403) se o valor estiver acima do
limite (`vaas.gate.limite-aprovacao-automatica`, hoje R$80.000). Isso é
defesa em profundidade: mesmo que o agente de IA "esqueça" de chamar o gate
antes, o domain service reforça a regra de qualquer forma. Ver o teste
`GatePoliticaServiceTest`.

## Próximos passos (fora do escopo deste exemplo)

1. **Expor via Apigee como MCP proxy** (ADR-001) — basta declarar o basepath
   `/mcp` sobre o OpenAPI já gerado por este projeto, sem reescrever nada aqui.
2. **Trocar a tabela de tributos em memória por Bedrock Knowledge Base**
   (ADR-003) — `TributosService` é o ponto de extensão.
3. **Trocar o `ApiKeyAuthFilter` pelo modelo real** de OAuth2 Token Exchange
   via BFF, descrito no documento "Fluxo de Requisição: MFE → Core".
4. **Substituir `CreditoService`** por chamada real aos squads de CaaS/LaaS
   — está aqui só para a demo end-to-end funcionar isoladamente.

## Estrutura

```
src/main/java/com/bradesco/vaas/
├── controller/   — REST controllers (1 por ferramenta MCP)
├── service/      — lógica de negócio sintética
├── dto/          — records de request/response
├── config/       — OpenAPI + filtro de API Key (placeholder)
└── exception/    — tratamento global de erros
(a documentação completa está em /docs/developer-portal, na raiz do showcase)
postman/          — coleção Postman pronta para importar
```
