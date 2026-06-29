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
