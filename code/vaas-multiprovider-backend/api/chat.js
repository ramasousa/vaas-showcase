// api/chat.js
// Função serverless (Vercel, runtime Node.js) — backend do Agente de Checkout Veicular.
// Recebe { provider, history, userMessage } e roda o loop agêntico completo
// (chamada ao modelo + execução de ferramentas sintéticas) inteiramente no servidor.
//
// Variáveis de ambiente necessárias (configurar no painel da Vercel):
//   ANTHROPIC_API_KEY
//   OPENAI_API_KEY
//   GEMINI_API_KEY

export default async function handler(req, res) {
  res.setHeader('Access-Control-Allow-Origin', '*'); // em produção, troque '*' pelo seu domínio
  res.setHeader('Access-Control-Allow-Methods', 'POST, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type');
  if (req.method === 'OPTIONS') return res.status(200).end();
  if (req.method !== 'POST') return res.status(405).json({ error: 'método não permitido' });

  const { provider, history = [], userMessage } = req.body || {};
  if (!provider || !userMessage) {
    return res.status(400).json({ error: 'provider e userMessage são obrigatórios' });
  }

  try {
    const result = await runAgent(provider, history, userMessage);
    return res.status(200).json(result);
  } catch (err) {
    console.error(err);
    return res.status(500).json({ error: err.message });
  }
}

// ---------------------------------------------------------------------------
// PROMPT E FERRAMENTAS (formato genérico — convertido pra cada provedor abaixo)
// ---------------------------------------------------------------------------

const SYSTEM_PROMPT = `Você é o Agente de Checkout Veicular do Bradesco. Ajuda o cliente a organizar a compra/pagamento de um veículo: dados do veículo, IPVA, licenciamento, emplacamento, financiamento (CaaS/LaaS) e pagamento.

Regras:
- Responda sempre em português do Brasil, de forma natural e objetiva (no máximo 3-4 frases por mensagem).
- Use as ferramentas disponíveis para obter dados — nunca invente valor de veículo, alíquota de imposto ou parcela.
- Se faltar placa e UF, pergunte antes de chamar consultar_veiculo.
- Antes de qualquer executar_pagamento, você DEVE chamar verificar_gate_politica primeiro. Só execute o pagamento se aprovado=true; se for false, explique que precisa de validação manual e não execute.
- Depois de calcular tributos, ofereça emplacamento via agendar_emplacamento quando fizer sentido.
- Seja proativo: depois de ter os dados, sugira o próximo passo em vez de só listar números.`;

const TOOLS_GENERIC = [
  { name: 'consultar_veiculo', description: 'Consulta dados sintéticos do veículo (modelo, ano, valor FIPE) a partir da placa e UF.',
    input_schema: { type: 'object', properties: { placa: { type: 'string' }, uf: { type: 'string', description: 'sigla do estado, ex: SP' } }, required: ['placa', 'uf'] } },
  { name: 'consultar_regra_tributaria', description: 'Calcula IPVA, licenciamento e taxa (transferência ou emplacamento inicial) para uma UF, considerando se o veículo é elétrico (incentivo) ou a combustão.',
    input_schema: { type: 'object', properties: { uf: { type: 'string' }, tipo_veiculo: { type: 'string', enum: ['combustao', 'eletrico'] }, valor_fipe: { type: 'number' }, zero_km: { type: 'boolean' } }, required: ['uf', 'tipo_veiculo', 'valor_fipe'] } },
  { name: 'simular_financiamento', description: 'Simula parcela de financiamento (CaaS/LaaS) para um valor e número de parcelas.',
    input_schema: { type: 'object', properties: { valor: { type: 'number' }, parcelas: { type: 'number' } }, required: ['valor', 'parcelas'] } },
  { name: 'verificar_gate_politica', description: 'Verifica deterministicamente (não é decisão do modelo) se um valor pode ser aprovado automaticamente. Limite: R$80.000. Sempre chamar antes de executar_pagamento.',
    input_schema: { type: 'object', properties: { valor_total: { type: 'number' } }, required: ['valor_total'] } },
  { name: 'executar_pagamento', description: 'Executa o pagamento (veículo ou tributo) — só após verificar_gate_politica retornar aprovado=true.',
    input_schema: { type: 'object', properties: { tipo: { type: 'string', enum: ['veiculo', 'tributo'] }, valor: { type: 'number' }, destinatario: { type: 'string' } }, required: ['tipo', 'valor', 'destinatario'] } },
  { name: 'agendar_emplacamento', description: 'Agenda a vistoria/emplacamento no DETRAN da UF informada.',
    input_schema: { type: 'object', properties: { uf: { type: 'string' }, placa: { type: 'string' } }, required: ['uf', 'placa'] } }
];

const TOOLS_CLAUDE = TOOLS_GENERIC.map(t => ({ name: t.name, description: t.description, input_schema: t.input_schema }));
const TOOLS_OPENAI = TOOLS_GENERIC.map(t => ({ type: 'function', function: { name: t.name, description: t.description, parameters: t.input_schema } }));
const TOOLS_GEMINI = TOOLS_GENERIC.map(t => ({ name: t.name, description: t.description, parameters: t.input_schema }));

const MAX_ROUNDS = 6;

// ---------------------------------------------------------------------------
// LOOP AGÊNTICO — um por provedor, mesma lógica, formatos nativos diferentes
// ---------------------------------------------------------------------------

async function runAgent(provider, history, userMessage) {
  const t0 = Date.now();
  const toolTrace = [];
  let finalText = '';

  if (provider === 'claude') {
    let messages = history.map(h => ({ role: h.role, content: h.text }));
    messages.push({ role: 'user', content: userMessage });
    for (let round = 0; round < MAX_ROUNDS; round++) {
      const data = await callClaudeAPI(messages);
      const blocks = data.content || [];
      const text = blocks.filter(b => b.type === 'text').map(b => b.text).join('\n');
      if (text) finalText += (finalText ? '\n' : '') + text;
      const toolUses = blocks.filter(b => b.type === 'tool_use');
      if (!toolUses.length) break;
      messages.push({ role: 'assistant', content: blocks });
      const toolResults = [];
      for (const tu of toolUses) {
        const out = await executeTool(tu.name, tu.input, toolTrace);
        toolResults.push({ type: 'tool_result', tool_use_id: tu.id, content: JSON.stringify(out) });
      }
      messages.push({ role: 'user', content: toolResults });
    }
  } else if (provider === 'gpt') {
    let messages = [{ role: 'system', content: SYSTEM_PROMPT }, ...history.map(h => ({ role: h.role, content: h.text })), { role: 'user', content: userMessage }];
    for (let round = 0; round < MAX_ROUNDS; round++) {
      const data = await callGPTAPI(messages);
      const msg = data.choices?.[0]?.message || {};
      if (msg.content) finalText += (finalText ? '\n' : '') + msg.content;
      const calls = msg.tool_calls || [];
      if (!calls.length) break;
      messages.push(msg);
      for (const tc of calls) {
        const input = JSON.parse(tc.function.arguments || '{}');
        const out = await executeTool(tc.function.name, input, toolTrace);
        messages.push({ role: 'tool', tool_call_id: tc.id, content: JSON.stringify(out) });
      }
    }
  } else if (provider === 'gemini') {
    let contents = history.map(h => ({ role: h.role === 'assistant' ? 'model' : 'user', parts: [{ text: h.text }] }));
    contents.push({ role: 'user', parts: [{ text: userMessage }] });
    for (let round = 0; round < MAX_ROUNDS; round++) {
      const data = await callGeminiAPI(contents);
      const parts = data.candidates?.[0]?.content?.parts || [];
      const text = parts.filter(p => p.text).map(p => p.text).join('\n');
      if (text) finalText += (finalText ? '\n' : '') + text;
      const calls = parts.filter(p => p.functionCall);
      if (!calls.length) break;
      contents.push({ role: 'model', parts });
      const responseParts = [];
      for (const c of calls) {
        const out = await executeTool(c.functionCall.name, c.functionCall.args, toolTrace);
        responseParts.push({ functionResponse: { name: c.functionCall.name, response: out } });
      }
      contents.push({ role: 'user', parts: responseParts });
    }
  } else {
    throw new Error('provider desconhecido: ' + provider);
  }

  return { provider, text: finalText.trim() || '(sem resposta de texto)', toolTrace, totalMs: Date.now() - t0 };
}

// ---------------------------------------------------------------------------
// CHAMADAS NATIVAS A CADA PROVEDOR
// ---------------------------------------------------------------------------

async function callClaudeAPI(messages) {
  const r = await fetch('https://api.anthropic.com/v1/messages', {
    method: 'POST',
    headers: { 'content-type': 'application/json', 'x-api-key': process.env.ANTHROPIC_API_KEY, 'anthropic-version': '2023-06-01' },
    body: JSON.stringify({ model: 'claude-sonnet-4-6', max_tokens: 1000, system: SYSTEM_PROMPT, tools: TOOLS_CLAUDE, messages })
  });
  if (!r.ok) throw new Error('Claude API HTTP ' + r.status + ': ' + await r.text());
  return r.json();
}

async function callGPTAPI(messages) {
  const r = await fetch('https://api.openai.com/v1/chat/completions', {
    method: 'POST',
    headers: { 'content-type': 'application/json', 'authorization': `Bearer ${process.env.OPENAI_API_KEY}` },
    body: JSON.stringify({ model: 'gpt-5.5', messages, tools: TOOLS_OPENAI })
  });
  if (!r.ok) throw new Error('OpenAI API HTTP ' + r.status + ': ' + await r.text());
  return r.json();
}

async function callGeminiAPI(contents) {
  const r = await fetch('https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent', {
    method: 'POST',
    headers: { 'content-type': 'application/json', 'x-goog-api-key': process.env.GEMINI_API_KEY },
    body: JSON.stringify({ system_instruction: { parts: [{ text: SYSTEM_PROMPT }] }, contents, tools: [{ function_declarations: TOOLS_GEMINI }] })
  });
  if (!r.ok) throw new Error('Gemini API HTTP ' + r.status + ': ' + await r.text());
  return r.json();
}

// ---------------------------------------------------------------------------
// EXECUÇÃO SINTÉTICA DAS FERRAMENTAS (idêntica em espírito ao protótipo anterior)
// ---------------------------------------------------------------------------

function hashStr(s) { let h = 0; for (let i = 0; i < s.length; i++) { h = (h * 31 + s.charCodeAt(i)) >>> 0; } return h; }
function delay(ms) { return new Promise(r => setTimeout(r, ms)); }

async function executeTool(name, input, trace) {
  const start = Date.now();
  await delay(150 + Math.random() * 350);
  let result, kind = 'mcp', detail = '';

  if (name === 'consultar_veiculo') {
    const models = [['Volkswagen Polo', 2023], ['Hyundai HB20', 2022], ['Fiat Pulse', 2023], ['Chevrolet Onix', 2024], ['Toyota Corolla', 2023], ['BYD Dolphin', 2026]];
    const seed = hashStr((input.placa || 'XXX0000') + (input.uf || ''));
    const m = models[seed % models.length];
    const fipe = 45000 + (seed % 95000);
    result = { modelo: m[0] + ' ' + m[1], placa: input.placa, uf: input.uf, valor_fipe: Math.round(fipe / 100) * 100 };
    detail = 'MCP → RENAVAM / FIPE (sintético)';
  } else if (name === 'consultar_regra_tributaria') {
    kind = 'rag';
    const rates = { SP: 4, RJ: 4, MG: 3 };
    let pct = rates[input.uf] || 4;
    if (input.tipo_veiculo === 'eletrico') pct = Math.max(1, Math.round(pct / 3));
    const ipva = Math.round(input.valor_fipe * pct / 100);
    const licensing = 150 + (hashStr(input.uf || '') % 100);
    const taxa = input.zero_km ? 200 + (hashStr((input.uf || '') + '0km') % 100) : 120 + (hashStr((input.uf || '') + 'usado') % 150);
    result = { ipva_pct: pct, ipva_valor: ipva, licenciamento: licensing, taxa, total_tributos: ipva + licensing + taxa, incentivo_eletrico: input.tipo_veiculo === 'eletrico' };
    detail = 'RAG → regra_tributaria_' + (input.uf || '??') + '_2026';
  } else if (name === 'simular_financiamento') {
    const parcela = Math.round(input.valor * 1.15 / input.parcelas);
    result = { parcela_mensal: parcela, parcelas: input.parcelas, taxa_am: '1.19%' };
    detail = 'MCP → CaaS/LaaS · motor de score';
  } else if (name === 'verificar_gate_politica') {
    kind = 'gate';
    const aprovado = input.valor_total <= 80000;
    result = { aprovado, limite: 80000, motivo: aprovado ? 'dentro do limite de auto-aprovação' : 'acima do limite — requer validação manual' };
    detail = 'Gate determinístico local — não é decisão do modelo';
  } else if (name === 'executar_pagamento') {
    const protocolo = 'BRD-2026-' + (10000 + Math.floor(Math.random() * 89999));
    result = { status: 'confirmado', protocolo, tipo: input.tipo, valor: input.valor };
    detail = 'MCP → liquidação (' + input.tipo + ')';
  } else if (name === 'agendar_emplacamento') {
    const protocolo = 'DETRAN-' + (input.uf || '??') + '-' + (10000 + Math.floor(Math.random() * 89999));
    result = { protocolo, data_vistoria: '12/07', uf: input.uf };
    detail = 'MCP → DETRAN-' + input.uf;
  } else {
    result = { erro: 'ferramenta desconhecida: ' + name };
    kind = 'audit'; detail = 'Chamada não reconhecida';
  }

  const ms = Date.now() - start;
  trace.push({ name, input, output: result, kind, detail, ms });
  return result;
}
