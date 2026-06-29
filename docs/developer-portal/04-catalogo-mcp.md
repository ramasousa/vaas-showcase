# Catálogo de Ferramentas MCP

Cada endpoint da API VaaS também é descrito aqui como uma ferramenta MCP —
o formato que um agente de IA usa para descobrir e decidir o que chamar.
Esses são os mesmos `input_schema` usados nos protótipos do agente real
(Claude/GPT/Gemini) e no system prompt de referência abaixo.

> Quando o domain service é publicado no Apigee com o basepath `/mcp`
> (ADR-001), este catálogo é gerado automaticamente a partir do
> `openapi.yaml` — o conteúdo abaixo é o contrato que isso precisa preservar.

## System prompt de referência

```
Você é o Agente de Checkout Veicular do Bradesco. Ajuda o cliente a
organizar a compra/pagamento de um veículo: dados do veículo, IPVA,
licenciamento, emplacamento, financiamento (CaaS/LaaS) e pagamento.

Regras:
- Responda sempre em português do Brasil, de forma natural e objetiva.
- Use as ferramentas disponíveis para obter dados — nunca invente valor
  de veículo, alíquota de imposto ou parcela.
- Se faltar placa e UF, pergunte antes de chamar consultar_veiculo.
- Antes de qualquer executar_pagamento, você DEVE chamar
  verificar_gate_politica primeiro. Só execute o pagamento se
  aprovado=true; se for false, explique que precisa de validação
  manual e não execute.
- Depois de calcular tributos, ofereça emplacamento via
  agendar_emplacamento quando fizer sentido.
```

## Ferramentas

### `consultar_veiculo`
```json
{
  "name": "consultar_veiculo",
  "description": "Consulta dados sintéticos do veículo (modelo, ano, valor FIPE) a partir da placa e UF.",
  "input_schema": {
    "type": "object",
    "properties": {
      "placa": { "type": "string" },
      "uf": { "type": "string", "description": "sigla do estado, ex: SP" }
    },
    "required": ["placa", "uf"]
  }
}
```

### `consultar_regra_tributaria`
```json
{
  "name": "consultar_regra_tributaria",
  "description": "Calcula IPVA, licenciamento e taxa (transferência ou emplacamento inicial) para uma UF, considerando se o veículo é elétrico (incentivo) ou a combustão.",
  "input_schema": {
    "type": "object",
    "properties": {
      "uf": { "type": "string" },
      "tipoVeiculo": { "type": "string", "enum": ["combustao", "eletrico"] },
      "valorFipe": { "type": "number" },
      "zeroKm": { "type": "boolean" }
    },
    "required": ["uf", "tipoVeiculo", "valorFipe"]
  }
}
```

### `simular_financiamento`
```json
{
  "name": "simular_financiamento",
  "description": "Simula parcela de financiamento (CaaS/LaaS) para um valor e número de parcelas.",
  "input_schema": {
    "type": "object",
    "properties": {
      "valor": { "type": "number" },
      "parcelas": { "type": "number" }
    },
    "required": ["valor", "parcelas"]
  }
}
```

### `verificar_gate_politica`
```json
{
  "name": "verificar_gate_politica",
  "description": "Verifica deterministicamente (não é decisão do modelo) se um valor pode ser aprovado automaticamente. Limite: R$80.000. Sempre chamar antes de executar_pagamento.",
  "input_schema": {
    "type": "object",
    "properties": { "valorTotal": { "type": "number" } },
    "required": ["valorTotal"]
  }
}
```

### `executar_pagamento`
```json
{
  "name": "executar_pagamento",
  "description": "Executa o pagamento (veículo ou tributo) — só deve ser chamada após verificar_gate_politica retornar aprovado=true.",
  "input_schema": {
    "type": "object",
    "properties": {
      "tipo": { "type": "string", "enum": ["veiculo", "tributo"] },
      "valor": { "type": "number" },
      "destinatario": { "type": "string" }
    },
    "required": ["tipo", "valor", "destinatario"]
  }
}
```

### `agendar_emplacamento`
```json
{
  "name": "agendar_emplacamento",
  "description": "Agenda a vistoria/emplacamento no DETRAN da UF informada.",
  "input_schema": {
    "type": "object",
    "properties": {
      "uf": { "type": "string" },
      "placa": { "type": "string" }
    },
    "required": ["uf", "placa"]
  }
}
```

## Regra de governança que não muda entre provedores

Independente do modelo por trás do agente (Claude, GPT, Gemini, ou — no
horizonte — Bridge IA / BIA Tech), o contrato acima e a regra de
"`verificar_gate_politica` sempre antes de `executar_pagamento`" são
exatamente os mesmos. Isso é o que permite trocar de provedor de modelo
sem reescrever a camada de ferramentas — ver o backend multi-provedor nos
protótipos já validados.
