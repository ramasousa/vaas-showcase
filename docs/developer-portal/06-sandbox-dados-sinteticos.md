# Sandbox & Dados Sintéticos

Todo dado retornado por este ambiente é gerado de forma **determinística**,
não consultado em nenhuma fonte real (RENAVAM, FIPE, SEFAZ, DETRAN). Isso é
intencional: permite testes repetíveis (a mesma placa sempre retorna o
mesmo veículo) sem depender de integrações externas ainda não construídas.

## Como cada dado é gerado

**Veículo (`consultar_veiculo`)** — a placa + UF passam por uma função de
hash simples; o resultado escolhe um modelo de uma lista fixa e gera um
valor FIPE dentro de uma faixa plausível (R$45.000–R$140.000). Mesma placa
→ mesmo veículo, sempre.

**Tributos (`consultar_regra_tributaria`)** — usamos uma tabela fixa de
alíquota de IPVA por UF:

| UF | Alíquota padrão | Alíquota com incentivo elétrico |
|---|---|---|
| SP | 4% | 1% |
| RJ | 4% | 1% |
| MG | 3% | 1% |
| outras | 4% (default) | 1% |

> Estas alíquotas são **ilustrativas**, não a legislação real de nenhum
> estado. Não usar como referência fiscal.

**Financiamento (`simular_financiamento`)** — fórmula simplificada
(`valor × 1,15 ÷ parcelas`), taxa fixa de 1,19% a.m. exibida apenas como
rótulo.

**Pagamento e emplacamento** — protocolos gerados aleatoriamente no formato
`BRD-2026-XXXXX` / `DETRAN-{UF}-XXXXX`, sem persistência — cada chamada
gera um protocolo novo.

## O que muda quando isso for para produção

| Hoje (sandbox) | Produção (ver ADRs) |
|---|---|
| Tabela de IPVA em memória | Bedrock Knowledge Base (RAG) — ADR-003 |
| Hash determinístico de placa | Integração real RENAVAM/FIPE |
| `CreditoService` local | Squads de CaaS/LaaS existentes |
| Protocolo aleatório | Integração real com convênio de arrecadação / DETRAN |
| `X-API-Key` estático | OAuth2 Token Exchange via BFF + Apigee |

Nenhuma dessas trocas deve exigir mudança de contrato (request/response) —
é por isso que vale manter os schemas deste sandbox estáveis desde já.
