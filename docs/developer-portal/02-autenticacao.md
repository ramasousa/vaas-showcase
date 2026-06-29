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
