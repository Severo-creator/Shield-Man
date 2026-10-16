package com.equipe.shieldman.domain.model;

import java.util.Objects;

/** Primeiro tipo de infograma: anotações ou regras em texto livre. */
public final class Documento extends Infograma {
    private String texto;

    public Documento(String titulo, String texto) {
        super(titulo);
        editarTexto(texto);
    }

    public String getTexto() {
        return texto;
    }

    public void editarTexto(String texto) {
        this.texto = Objects.requireNonNull(texto, "Texto deve ser informado; pode ser vazio.");
    }
}
