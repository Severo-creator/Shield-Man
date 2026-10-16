package com.equipe.shieldman.domain.model;

import java.util.Objects;
import java.util.UUID;

/** Uma célula, identificada pela linha e pela coluna da mesma tabela. */
public final class ValorCampo {
    private final UUID linhaId;
    private final UUID colunaId;
    private String campo;

    ValorCampo(UUID linhaId, UUID colunaId, String campo) {
        this.linhaId = Objects.requireNonNull(linhaId);
        this.colunaId = Objects.requireNonNull(colunaId);
        editar(campo);
    }

    public UUID getLinhaId() { return linhaId; }
    public UUID getColunaId() { return colunaId; }
    public String getCampo() { return campo; }

    void editar(String campo) {
        this.campo = Objects.requireNonNull(campo, "Campo pode ser vazio, mas não nulo.");
    }
}
