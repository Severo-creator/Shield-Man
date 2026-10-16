package com.equipe.shieldman.domain.model;

import java.util.Objects;
import java.util.UUID;

/** Intervalo inclusivo de resultados associado a uma linha da tabela rolável. */
public final class ValorChance {
    private final UUID linhaId;
    private int valorMinimo;
    private int valorMaximo;

    ValorChance(UUID linhaId, int minimo, int maximo) {
        this.linhaId = Objects.requireNonNull(linhaId);
        redefinirChance(minimo, maximo);
    }

    public UUID getLinhaId() { return linhaId; }
    public int getValorMinimo() { return valorMinimo; }
    public int getValorMaximo() { return valorMaximo; }

    // A tabela controla a alteração para impedir sobreposição entre linhas.
    void redefinirChance(int minimo, int maximo) {
        if (minimo < 1 || maximo < minimo) {
            throw new IllegalArgumentException("A faixa deve começar em 1 ou mais e ter mínimo <= máximo.");
        }
        valorMinimo = minimo;
        valorMaximo = maximo;
    }

    public boolean contem(int resultado) {
        return resultado >= valorMinimo && resultado <= valorMaximo;
    }
}
