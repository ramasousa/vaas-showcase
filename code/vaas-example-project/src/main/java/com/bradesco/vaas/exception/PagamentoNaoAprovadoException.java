package com.bradesco.vaas.exception;

/**
 * Lançada quando {@code executar_pagamento} é chamado sem que o
 * {@code verificar_gate_politica} correspondente tenha aprovado o valor.
 * Isso nunca deveria acontecer se o agente seguir o system prompt
 * corretamente — mas a aplicação NÃO confia nisso: a regra é sempre
 * reforçada aqui, no domain service, não só no prompt do modelo.
 */
public class PagamentoNaoAprovadoException extends RuntimeException {
    public PagamentoNaoAprovadoException(String message) {
        super(message);
    }
}
