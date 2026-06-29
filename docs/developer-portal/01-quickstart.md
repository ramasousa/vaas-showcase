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
