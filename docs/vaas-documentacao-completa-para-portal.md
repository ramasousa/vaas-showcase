# VaaS — Documentação Completa para o Developer Portal

> Documento consolidado a partir do projeto de exemplo VaaS, pronto para ser
> colado/anexado no chat onde o Developer Portal está sendo construído.
> Pedido sugerido para aquele chat: adicionar VaaS como novo produto,
> seguindo o template de 7 seções obrigatórias e o template OpenAPI 3.1 já
> usados para CaaS/Seguros/Consórcio, versionando o portal antes de editar.

## Índice

1. Visão Geral
2. Quickstart
3. Autenticação & Segurança
4. Referência de API
5. Catálogo de Ferramentas MCP
6. Sandbox & Dados Sintéticos
7. Changelog
8. Especificação OpenAPI 3.1 (anexo)

---

# VaaS — Vehicle as a Service

> **Status:** Sandbox · v0.1.0 · Fase 1 do roadmap de maturidade

VaaS é a camada de domínio que orquestra a jornada completa de checkout
veicular dentro do ecossistema Bradesco: pagamento do veículo, cálculo e
pagamento de tributos (IPVA, licenciamento), simulação de financiamento e
agendamento de emplacamento — tudo no mesmo lugar, com IA decidindo a
sequência de chamadas e um gate determinístico protegendo toda execução
financeira.

## Por que isso existe

No fluxo tradicional, o cliente compra o veículo no banco e some pra
resolver IPVA, emplacamento e transferência em sistemas fragmentados fora
do Bradesco. VaaS fecha esse ciclo: a mesma infraestrutura de APIs que já
sustenta BaaS, CaaS e LaaS agora cobre o universo veicular de ponta a ponta.

## Onde VaaS se encaixa na plataforma

```
BaaS  → conta, pagamentos, KYC
CaaS  → crédito embedded simplificado
LaaS  → crédito estruturado (financiamento de maior ticket)
VaaS  → domínio veicular — consome CaaS/LaaS para crédito, BaaS para
         expor a parceiros (concessionárias, marketplaces, locadoras de frota)
```

## Como a IA participa

Cada endpoint deste produto também existe como uma **ferramenta MCP** —
um agente de IA pode descobrir e chamar `consultar_veiculo`,
`consultar_regra_tributaria`, `simular_financiamento` etc. sem integração
ponto a ponto. Ver [Catálogo de Ferramentas MCP](./04-catalogo-mcp.md).

Toda execução financeira passa por um **gate de política determinístico**
antes de acontecer — isso nunca é decisão do modelo de IA, é uma regra fixa
reforçada no próprio domain service. Ver
[Autenticação & Segurança](./02-autenticacao.md).

## Por onde começar

1. [Quickstart (&lt;10 min)](./01-quickstart.md) — primeira chamada em poucos minutos
2. [Referência de API](./03-referencia-api.md) — todos os endpoints, com exemplos
3. [Catálogo de Ferramentas MCP](./04-catalogo-mcp.md) — para times construindo agentes
4. [Sandbox & Dados Sintéticos](./06-sandbox-dados-sinteticos.md) — como os dados de teste são gerados

## Produtos relacionados neste portal

- **CaaS** — crédito embedded
- **LaaS** — crédito estruturado
- **BaaS + MCP** — exposição de APIs a parceiros
- **Seguros** — em integração com VaaS na Fase 4 (seguro usage-based para frotas)
- **Débito Veicular** — arquitetura multi-UF que o VaaS reaproveita
-e 

---


# Quickstart — primeira chamada em menos de 10 minutos

## 1. Suba o ambiente local

```bash
git clone <repo-do-projeto-de-exemplo>
cd vaas-example-project
mvn spring-boot:run
```

A API sobe em `http://localhost:8080`. Todas as chamadas exigem o header:

```
X-API-Key: demo-key-troque-em-producao
```

## 2. Faça a jornada completa, passo a passo

**Consultar o veículo:**
```bash
curl -X GET "http://localhost:8080/v1/veiculos/ABC1D23?uf=SP" \
  -H "X-API-Key: demo-key-troque-em-producao"
```

**Calcular tributos:**
```bash
curl -X POST "http://localhost:8080/v1/tributos/calcular" \
  -H "X-API-Key: demo-key-troque-em-producao" \
  -H "Content-Type: application/json" \
  -d '{"uf":"SP","tipoVeiculo":"combustao","valorFipe":72400,"zeroKm":false}'
```

**Verificar o gate de política** (sempre antes de pagar):
```bash
curl -X POST "http://localhost:8080/v1/gate/politica" \
  -H "X-API-Key: demo-key-troque-em-producao" \
  -H "Content-Type: application/json" \
  -d '{"valorTotal":75795}'
```

**Executar o pagamento** (só se o gate acima retornou `aprovado: true`):
```bash
curl -X POST "http://localhost:8080/v1/pagamentos" \
  -H "X-API-Key: demo-key-troque-em-producao" \
  -H "Content-Type: application/json" \
  -d '{"tipo":"veiculo","valor":72400,"destinatario":"vendedor-exemplo"}'
```

**Agendar o emplacamento:**
```bash
curl -X POST "http://localhost:8080/v1/emplacamentos" \
  -H "X-API-Key: demo-key-troque-em-producao" \
  -H "Content-Type: application/json" \
  -d '{"uf":"SP","placa":"ABC1D23"}'
```

## 3. Veja o que acontece quando o gate bloqueia

Repita a chamada de pagamento com um valor acima de R$80.000 — você recebe
`403 Forbidden` com o código `PAGAMENTO_BLOQUEADO_PELO_GATE`, mesmo sem ter
chamado o gate antes. Essa é a defesa em profundidade descrita em
[Autenticação & Segurança](./02-autenticacao.md).

## 4. Importe a coleção Postman

O arquivo `postman/VaaS.postman_collection.json` já tem as 7 chamadas acima
prontas, incluindo o caso de bloqueio pelo gate.

## Próximo passo

Leia [Catálogo de Ferramentas MCP](./04-catalogo-mcp.md) se o seu time está
construindo um agente que consome essas APIs como ferramentas, em vez de
chamá-las diretamente.
-e 

---


# Autenticação & Segurança

## No ambiente de exemplo (este repositório)

Por simplicidade, o projeto de exemplo valida só um header estático:

```
X-API-Key: demo-key-troque-em-producao
```

Isso existe **apenas para o setup local funcionar sem dependências
externas**. Não é o modelo de segurança real, e não deve ser usado como
referência para produção.

## No modelo real de produção

A autenticação segue o fluxo descrito no documento interno
**"Fluxo de Requisição: MFE → Core"**. Resumo dos pontos que todo
consumidor desta API precisa entender:

1. **O token do usuário final morre no BFF.** O micro-frontend (chat,
   widget embeddable) nunca chama o domain service diretamente — ele passa
   pelo BFF, que troca o JWT de sessão do usuário por um token de serviço
   com escopo restrito (OAuth2 Token Exchange, RFC 8693).
2. **Daqui pra baixo, é tudo token de serviço.** O Apigee valida esse token
   de serviço, aplica quota/rate limit, e só então roteia para o domain
   service real (este projeto) — via mTLS ou OAuth2 client credentials.
3. **O domain service nunca recebe credencial de cliente final.** Se você
   está integrando um novo consumidor a este serviço, ele deve estar atrás
   do Apigee, nunca exposto diretamente.

## O Gate de Política não é negociável

Diferente da autenticação (que pode evoluir), o **gate de política é uma
regra fixa, reforçada no código do domain service** — não apenas no prompt
do agente de IA. Veja `GatePoliticaService` e `PagamentoService` no
projeto de exemplo: mesmo que o agente "esqueça" de chamar
`verificar_gate_politica` antes de `executar_pagamento`, o serviço de
pagamento chama o gate internamente e bloqueia a operação.

Isso é intencional e não deve ser removido em nenhuma evolução futura desta
API — é a peça que torna seguro dar autonomia de orquestração a um agente
de IA.

## Guardrails na camada de IA

Quando este serviço é chamado a partir do agente (via MCP), o conteúdo que
chega e sai do modelo passa por Guardrails (filtro de conteúdo, detecção de
prompt injection, verificação de grounding) antes e depois da chamada —
isso acontece na camada de orquestração de IA, não neste domain service.
Ver "Arquitetura Técnica" no documento de execução para o desenho completo.
-e 

---


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
-e 

---


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
-e 

---


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
-e 

---


# Changelog — VaaS API

## v0.1.0 — Sandbox inicial

- Primeira versão pública no developer portal.
- 6 endpoints: `consultar_veiculo`, `consultar_regra_tributaria`,
  `simular_financiamento`, `verificar_gate_politica`, `executar_pagamento`,
  `agendar_emplacamento`.
- Catálogo de ferramentas MCP publicado, alinhado 1:1 com os endpoints REST.
- Gate de política com limite fixo de R$80.000, reforçado tanto no
  catálogo MCP quanto no domain service (defesa em profundidade).
- Dados 100% sintéticos — ver "Sandbox & Dados Sintéticos".
- Suporte a UF: SP, RJ, MG (demais UFs usam alíquota padrão de 4%).

### Conhecido / em aberto
- Autenticação ainda é placeholder (`X-API-Key` estático) — modelo real de
  Token Exchange depende do ADR de segurança ainda não fechado.
- `simular_financiamento` é um stub — será substituído pela integração
  real com CaaS/LaaS antes do piloto controlado (Fase 2 do roadmap).
- Sem suporte ainda às variáveis de frota (telemetria, manutenção
  preditiva, seguro usage-based) — previsto para a Fase 4.
-e 

---


## 8. Especificação OpenAPI 3.1 (anexo)

```yaml
openapi: 3.1.0
info:
  title: VaaS API — Vehicle as a Service
  version: "0.1.0"
  description: |
    APIs de domínio do VaaS (Vehicle as a Service) — Bradesco Open Platform.

    Cobre a jornada de checkout veicular: consulta de veículo, cálculo de
    tributos por UF, simulação de financiamento, gate de política, execução
    de pagamento e agendamento de emplacamento.

    > Todos os dados retornados pelo ambiente de sandbox são **sintéticos**.
    > Ver "Sandbox & Dados Sintéticos" neste portal.

    Cada operação abaixo corresponde a uma ferramenta MCP do catálogo do
    agente — ver "Catálogo de Ferramentas MCP".
  contact:
    name: Open Platform — APIs, BaaS & Embedded Finance
    email: open-platform@bradesco.com.br
servers:
  - url: https://sandbox.api.bradesco.com.br/vaas/v1
    description: Sandbox (dados sintéticos)
  - url: http://localhost:8080/v1
    description: Ambiente local (projeto de exemplo)

security:
  - ApiKeyAuth: []

paths:
  /veiculos/{placa}:
    get:
      operationId: consultarVeiculo
      summary: Consulta dados do veículo
      tags: [Veículo]
      parameters:
        - name: placa
          in: path
          required: true
          schema: { type: string, example: ABC1D23 }
        - name: uf
          in: query
          required: true
          schema: { type: string, example: SP }
      responses:
        "200":
          description: Veículo encontrado
          content:
            application/json:
              schema: { $ref: "#/components/schemas/VeiculoResponse" }

  /tributos/calcular:
    post:
      operationId: calcularTributos
      summary: Calcula IPVA, licenciamento e taxa para uma UF
      tags: [Tributos]
      requestBody:
        required: true
        content:
          application/json:
            schema: { $ref: "#/components/schemas/RegraTributariaRequest" }
      responses:
        "200":
          description: Cálculo realizado
          content:
            application/json:
              schema: { $ref: "#/components/schemas/RegraTributariaResponse" }

  /credito/simular:
    post:
      operationId: simularFinanciamento
      summary: Simula parcela de financiamento (CaaS/LaaS)
      tags: [Crédito]
      requestBody:
        required: true
        content:
          application/json:
            schema: { $ref: "#/components/schemas/FinanciamentoRequest" }
      responses:
        "200":
          description: Simulação realizada
          content:
            application/json:
              schema: { $ref: "#/components/schemas/FinanciamentoResponse" }

  /gate/politica:
    post:
      operationId: verificarGatePolitica
      summary: Verifica aprovação automática determinística de um valor
      tags: [Gate de Política]
      requestBody:
        required: true
        content:
          application/json:
            schema: { $ref: "#/components/schemas/GatePoliticaRequest" }
      responses:
        "200":
          description: Resultado do gate
          content:
            application/json:
              schema: { $ref: "#/components/schemas/GatePoliticaResponse" }

  /pagamentos:
    post:
      operationId: executarPagamento
      summary: Executa pagamento (veículo ou tributo)
      tags: [Pagamento]
      requestBody:
        required: true
        content:
          application/json:
            schema: { $ref: "#/components/schemas/PagamentoRequest" }
      responses:
        "200":
          description: Pagamento confirmado
          content:
            application/json:
              schema: { $ref: "#/components/schemas/PagamentoResponse" }
        "403":
          description: Bloqueado pelo gate de política
          content:
            application/json:
              schema: { $ref: "#/components/schemas/ErrorResponse" }

  /emplacamentos:
    post:
      operationId: agendarEmplacamento
      summary: Agenda vistoria/emplacamento no DETRAN da UF
      tags: [Emplacamento]
      requestBody:
        required: true
        content:
          application/json:
            schema: { $ref: "#/components/schemas/EmplacamentoRequest" }
      responses:
        "200":
          description: Agendamento confirmado
          content:
            application/json:
              schema: { $ref: "#/components/schemas/EmplacamentoResponse" }

components:
  securitySchemes:
    ApiKeyAuth:
      type: apiKey
      in: header
      name: X-API-Key

  schemas:
    VeiculoResponse:
      type: object
      properties:
        placa: { type: string, example: ABC1D23 }
        uf: { type: string, example: SP }
        modelo: { type: string, example: "Volkswagen Polo 2023" }
        valorFipe: { type: number, example: 72400 }

    RegraTributariaRequest:
      type: object
      required: [uf, tipoVeiculo, valorFipe]
      properties:
        uf: { type: string, example: SP }
        tipoVeiculo: { type: string, enum: [combustao, eletrico] }
        valorFipe: { type: number, example: 72400 }
        zeroKm: { type: boolean, default: false }

    RegraTributariaResponse:
      type: object
      properties:
        ipvaPercentual: { type: integer, example: 4 }
        ipvaValor: { type: number, example: 2896 }
        licenciamento: { type: number, example: 187 }
        taxa: { type: number, example: 312 }
        totalTributos: { type: number, example: 3395 }
        incentivoEletrico: { type: boolean }

    FinanciamentoRequest:
      type: object
      required: [valor, parcelas]
      properties:
        valor: { type: number, example: 72400 }
        parcelas: { type: integer, example: 48 }

    FinanciamentoResponse:
      type: object
      properties:
        parcelaMensal: { type: number, example: 1735.08 }
        parcelas: { type: integer, example: 48 }
        taxaAm: { type: string, example: "1.19%" }

    GatePoliticaRequest:
      type: object
      required: [valorTotal]
      properties:
        valorTotal: { type: number, example: 75795 }

    GatePoliticaResponse:
      type: object
      properties:
        aprovado: { type: boolean }
        limite: { type: number, example: 80000 }
        motivo: { type: string, example: "dentro do limite de auto-aprovação" }

    PagamentoRequest:
      type: object
      required: [tipo, valor, destinatario]
      properties:
        tipo: { type: string, enum: [veiculo, tributo] }
        valor: { type: number, example: 72400 }
        destinatario: { type: string, example: "vendedor-exemplo" }

    PagamentoResponse:
      type: object
      properties:
        status: { type: string, example: confirmado }
        protocolo: { type: string, example: "BRD-2026-88341" }
        tipo: { type: string, example: veiculo }
        valor: { type: number, example: 72400 }

    EmplacamentoRequest:
      type: object
      required: [uf, placa]
      properties:
        uf: { type: string, example: SP }
        placa: { type: string, example: ABC1D23 }

    EmplacamentoResponse:
      type: object
      properties:
        protocolo: { type: string, example: "DETRAN-SP-90112" }
        dataVistoria: { type: string, example: "05/07" }
        uf: { type: string, example: SP }

    ErrorResponse:
      type: object
      properties:
        codigo: { type: string, example: PAGAMENTO_BLOQUEADO_PELO_GATE }
        mensagem: { type: string }

tags:
  - name: Veículo
  - name: Tributos
  - name: Crédito
  - name: Gate de Política
  - name: Pagamento
  - name: Emplacamento
```
