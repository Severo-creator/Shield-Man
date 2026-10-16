package com.equipe.shieldman.domain.model;

import java.util.UUID;

/** Base dos conteúdos consultáveis: documentos e, futuramente, tabelas e fichas. */
public abstract class Infograma {
    private final UUID id = UUID.randomUUID();
    private String titulo;

    protected Infograma(String titulo) {
        renomear(titulo);
    }

    public UUID getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void renomear(String titulo) {
        this.titulo = Validacao.textoObrigatorio(titulo, "Título do infograma");
    }
}
