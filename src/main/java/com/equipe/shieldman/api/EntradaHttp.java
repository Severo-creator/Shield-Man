package com.equipe.shieldman.api;

import java.util.UUID;
import jakarta.ws.rs.core.UriInfo;

final class EntradaHttp {
    static final String UUID_REGEX = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}";
    private EntradaHttp() { }

    static UUID uuid(String texto) {
        try {
            UUID id = UUID.fromString(texto);
            if (!id.toString().equalsIgnoreCase(texto)) { throw new IllegalArgumentException(); }
            return id;
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new IllegalArgumentException("Identificador deve ser um UUID válido.");
        }
    }

    static <T> T corpo(T corpo) {
        if (corpo == null) { throw new IllegalArgumentException("Informe um corpo JSON."); }
        return corpo;
    }

    static void parametros(UriInfo uri) {
        // O Quarkus pode converter um parâmetro vazio em null ou no valor padrão.
        // A URI distingue a ausência legítima de um valor fornecido vazio.
        for (String nome : new String[]{"pagina", "tamanho", "mestreId"}) {
            var valores = uri.getQueryParameters().get(nome);
            if (valores != null && (valores.size() != 1 || valores.getFirst() == null || valores.getFirst().isBlank())) {
                throw new IllegalArgumentException("Parâmetro " + nome + " deve ter um único valor não vazio.");
            }
        }
    }
}
