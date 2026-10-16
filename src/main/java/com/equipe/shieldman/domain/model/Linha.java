package com.equipe.shieldman.domain.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public final class Linha {
    private final UUID id = UUID.randomUUID();
    private final int ordem;
    private final List<ValorCampo> campos = new ArrayList<>();

    Linha(int ordem) { this.ordem = ordem; }

    public UUID getId() { return id; }
    public int getOrdem() { return ordem; }
    public List<ValorCampo> getCampos() { return List.copyOf(campos); }

    void adicionarCampo(ValorCampo campo) { campos.add(campo); }

    public String gerarTexto() {
        return campos.stream().map(ValorCampo::getCampo).collect(Collectors.joining(" | "));
    }
}
