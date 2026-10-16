package com.equipe.shieldman.domain.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class Coluna {
    private final UUID id = UUID.randomUUID();
    private final int ordem;
    private String titulo;
    private final List<ValorCampo> campos = new ArrayList<>();

    Coluna(String titulo, int ordem) {
        renomear(titulo);
        this.ordem = ordem;
    }

    public UUID getId() { return id; }
    public int getOrdem() { return ordem; }
    public String getTitulo() { return titulo; }
    public List<ValorCampo> getCampos() { return List.copyOf(campos); }

    public void renomear(String titulo) {
        this.titulo = Validacao.textoObrigatorio(titulo, "Título da coluna");
    }

    void adicionarCampo(ValorCampo campo) { campos.add(campo); }
}
