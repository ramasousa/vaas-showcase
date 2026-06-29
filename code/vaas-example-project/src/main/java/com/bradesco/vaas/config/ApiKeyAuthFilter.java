package com.bradesco.vaas.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * ATENÇÃO — isto é um PLACEHOLDER, não o modelo de segurança real.
 *
 * Neste projeto de exemplo, validamos só um header estático (X-API-Key)
 * pra simular "alguma autenticação" sem complicar o setup local. Em
 * produção, este serviço NUNCA recebe credencial de usuário final — ele
 * só aceita chamadas autenticadas via mTLS/OAuth2 client credentials,
 * vindas do MCP proxy do Apigee (ver passo 7 do documento
 * "Fluxo de Requisição: MFE → Core").
 *
 * Endpoints de documentação (/swagger-ui, /v3/api-docs) ficam liberados.
 */
@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    @Value("${vaas.security.api-key:demo-key-troque-em-producao}")
    private String expectedApiKey;

    private static final String[] PUBLIC_PATHS = {
            "/swagger-ui", "/v3/api-docs", "/actuator/health"
    };

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        boolean isPublic = false;
        for (String p : PUBLIC_PATHS) {
            if (path.startsWith(p)) { isPublic = true; break; }
        }

        if (isPublic) {
            chain.doFilter(request, response);
            return;
        }

        String apiKey = request.getHeader("X-API-Key");
        if (expectedApiKey.equals(apiKey)) {
            chain.doFilter(request, response);
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("""
                    {"codigo":"NAO_AUTORIZADO","mensagem":"X-API-Key ausente ou inválida. Ver docs/02-autenticacao.md."}
                    """);
        }
    }
}
