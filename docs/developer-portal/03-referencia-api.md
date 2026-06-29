# Referência de API

> Spec completa em [`openapi.yaml`](./openapi.yaml) — importável no Postman,
> no Apigee, ou em qualquer ferramenta que leia OpenAPI 3.1.

## `GET /v1/veiculos/{placa}`

Consulta dados sintéticos do veículo (equivalente a uma consulta RENAVAM/FIPE real).

**Query params:** `uf` (obrigatório, ex: `SP`)

**Resposta `200`:**
```json
{
  "placa": "ABC1D23",
  "uf": "SP",
  "modelo": "Volkswagen Polo 2023",
  "valorFipe": 72400
}
```

---

## `POST /v1/tributos/calcular`

Calcula IPVA, licenciamento e taxa (transferência ou emplacamento inicial)
para uma UF. Veículos elétricos recebem alíquota de IPVA reduzida
automaticamente.

**Corpo:**
```json
{ "uf": "SP", "tipoVeiculo": "combustao", "valorFipe": 72400, "zeroKm": false }
```

**Resposta `200`:**
```json
{
  "ipvaPercentual": 4,
  "ipvaValor": 2896,
  "licenciamento": 187,
  "taxa": 312,
  "totalTributos": 3395,
  "incentivoEletrico": false
}
```

---

## `POST /v1/credito/simular`

Simula parcela de financiamento. **Em produção, este endpoint não existe
no domínio VaaS** — a simulação real vem dos squads de CaaS (ticket menor,
embedded) ou LaaS (ticket maior, estruturado) já existentes na Tribe BaaS.
Está aqui só para a demonstração end-to-end funcionar isoladamente.

**Corpo:** `{ "valor": 72400, "parcelas": 48 }`

**Resposta `200`:** `{ "parcelaMensal": 1735.08, "parcelas": 48, "taxaAm": "1.19%" }`

---

## `POST /v1/gate/politica`

Verifica, de forma **determinística**, se um valor pode ser aprovado
automaticamente. Limite padrão: R$80.000 (configurável).

**Corpo:** `{ "valorTotal": 75795 }`

**Resposta `200`:** `{ "aprovado": true, "limite": 80000, "motivo": "dentro do limite de auto-aprovação" }`

> Chame sempre este endpoint antes de `POST /v1/pagamentos` — mas saiba que
> o próprio `/v1/pagamentos` reverifica isso internamente, então pular essa
> chamada não cria um risco de segurança, só perde a chance de explicar ao
> usuário *antes* de tentar pagar.

---

## `POST /v1/pagamentos`

Executa o pagamento (veículo ou tributo). Retorna **`403`** se o gate de
política não aprovar — mesmo que você não tenha chamado `/v1/gate/politica`
antes.

**Corpo:** `{ "tipo": "veiculo", "valor": 72400, "destinatario": "vendedor-exemplo" }`

**Resposta `200`:** `{ "status": "confirmado", "protocolo": "BRD-2026-88341", "tipo": "veiculo", "valor": 72400 }`

**Resposta `403`:**
```json
{ "codigo": "PAGAMENTO_BLOQUEADO_PELO_GATE", "mensagem": "Pagamento de valor 150000 bloqueado pelo gate de política: acima do limite — requer validação manual" }
```

---

## `POST /v1/emplacamentos`

Agenda vistoria/emplacamento no DETRAN da UF informada.

**Corpo:** `{ "uf": "SP", "placa": "ABC1D23" }`

**Resposta `200`:** `{ "protocolo": "DETRAN-SP-90112", "dataVistoria": "05/07", "uf": "SP" }`
