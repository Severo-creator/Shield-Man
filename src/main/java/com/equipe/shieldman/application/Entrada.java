package com.equipe.shieldman.application;

import java.util.Locale;
import java.util.UUID;

public final class Entrada {
    private Entrada() { }

    public static String nome(String nome, int maximo) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome deve ser informado.");
        }
        String limpo = nome.strip();
        if (limpo.length() > maximo) {
            throw new IllegalArgumentException("Nome deve ter até " + maximo + " caracteres.");
        }
        return limpo;
    }

    public static UUID id(UUID id) {
        if (id == null) { throw new IllegalArgumentException("Identificador deve ser informado."); }
        return id;
    }

    public static void paginacao(int pagina, int tamanho) {
        if (pagina < 0 || tamanho < 1 || tamanho > 100 || (long) pagina * tamanho > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Página deve ser não negativa e tamanho deve estar entre 1 e 100.");
        }
    }

    public static String filtro(String nome) {
        if (nome == null) { return ""; }
        if (nome.length() > 160) { throw new IllegalArgumentException("Filtro de nome muito longo."); }
        return nome.strip().toLowerCase(Locale.ROOT);
    }
}
