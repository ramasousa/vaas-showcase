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
