# VaaS — Vehicle as a Service · Showcase Completo

Repositório de apoio da iniciativa VaaS (Open Platform — APIs, BaaS &
Embedded Finance). Reúne tese estratégica, arquitetura de execução, seis
protótipos navegáveis, documentação de developer portal e código real de
exemplo (Java/Spring Boot).

## Como publicar no GitHub Pages

1. Crie um repositório novo e suba todo o conteúdo desta pasta na raiz
   (incluindo o `index.html`).
2. No GitHub: **Settings → Pages → Build and deployment → Source: Deploy
   from a branch → Branch: `main` / `root`**.
3. Em alguns minutos, o site fica disponível em
   `https://<seu-usuario>.github.io/<nome-do-repo>/`.

Todos os protótipos (`.html`) são arquivos únicos e autocontidos (CSS/JS
inline, só dependem de Google Fonts via CDN) — funcionam direto pelo
GitHub Pages, sem build step.

## Uma ressalva sobre os arquivos `.md`

O GitHub **renderiza Markdown automaticamente ao navegar o repositório**
em github.com (ex: abrir `docs/developer-portal/00-visao-geral.md` lá
mostra a página formatada). Já pela **URL publicada do GitHub Pages**,
arquivos `.md` são servidos como texto puro, a menos que você configure
Jekyll (que o GitHub Pages já suporta nativamente) com front-matter em
cada arquivo. Pra documentação que você queira navegável como página
formatada no Pages, a opção mais simples é converter para `.html` —
posso gerar isso se for útil.

## ⚠️ O protótipo "Agente Multi-Provedor" precisa de backend

`prototipos/agente-multiprovedor.html` chama `/api/chat` — isso só
funciona se você publicar `code/vaas-multiprovider-backend` na Vercel
(ou outro host com função serverless) e apontar a constante `API_BASE`
no topo do arquivo pra essa URL. Sozinho no GitHub Pages, ele mostra erro
de conexão — comportamento esperado, não é bug.

## Estrutura

```
.
├── index.html                          ← hub central, comece por aqui
├── estrategia/
│   ├── showcase-estrategico.html
│   └── arquitetura-execucao.html
├── prototipos/
│   ├── chat-orquestracao-multiuf.html
│   ├── widget-concessionaria.html
│   ├── marketplace-byd.html
│   ├── agente-real-claude.html
│   ├── agente-multiprovedor.html       ← requer backend, ver acima
│   └── frota-telemetria.html
├── docs/
│   ├── developer-portal/               ← 7 seções + openapi.yaml
│   └── vaas-documentacao-completa-para-portal.md
└── code/
    ├── vaas-example-project/           ← Java 21 / Spring Boot 3.3
    └── vaas-multiprovider-backend/     ← função serverless (Vercel)
```

## Histórico de versões deste showcase

Os protótipos abaixo passaram por iterações durante o desenvolvimento
(principalmente correções de layout mobile) — este repositório já traz
só a versão final e corrigida de cada um:

- Chat + Orquestração: evoluiu de 1 UF → 2 UFs → 3 UFs (SP/RJ/MG)
- Widget Embedded: ganhou o painel de orquestração numa segunda rodada
- Jornada Marketplace: teve 4 correções de bug de viewport no mobile

Nenhuma versão intermediária foi incluída aqui — só o estado final.
