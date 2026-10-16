package com.equipe.shieldman.api;

import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import jakarta.ws.rs.core.Application;

@OpenAPIDefinition(info = @Info(title = "ShieldMan API", version = "1.0.0",
        description = "Cadastros de mestres e escudos. Listagens com filtros e paginação; "
                + "erros em application/problem+json. Os conteúdos do painel ainda não possuem rotas de persistência."))
public class ApiDocumentacao extends Application {
}
