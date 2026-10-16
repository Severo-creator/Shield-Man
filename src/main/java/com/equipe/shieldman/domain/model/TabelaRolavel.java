package com.equipe.shieldman.domain.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.random.RandomGenerator;

public final class TabelaRolavel extends Tabela {
    private final Map<UUID, ValorChance> chances = new LinkedHashMap<>();

    public TabelaRolavel(String titulo) { super(titulo); }

    public List<ValorChance> getChances() { return List.copyOf(chances.values()); }

    public void definirChance(UUID linhaId, int minimo, int maximo) {
        buscarLinha(linhaId);
        ValorChance nova = new ValorChance(linhaId, minimo, maximo);
        boolean sobreposta = chances.values().stream()
                .filter(chance -> !chance.getLinhaId().equals(linhaId))
                .anyMatch(chance -> minimo <= chance.getValorMaximo() && maximo >= chance.getValorMinimo());
        if (sobreposta) {
            throw new IllegalArgumentException("Faixa de resultados sobreposta à de outra linha.");
        }
        ValorChance existente = chances.get(linhaId);
        if (existente == null) {
            chances.put(linhaId, nova);
        } else {
            existente.redefinirChance(minimo, maximo);
        }
    }

    public String resolverResultado(int resultado) {
        ValorChance chance = chances.values().stream().filter(item -> item.contem(resultado)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Resultado sem uma linha associada."));
        return buscarLinha(chance.getLinhaId()).gerarTexto();
    }

    public String rolarResultado() { return rolarResultado(RandomGenerator.getDefault()); }

    public String rolarResultado(RandomGenerator random) {
        Objects.requireNonNull(random);
        if (chances.isEmpty() || chances.size() != getLinhas().size()) {
            throw new IllegalStateException("Configure a faixa de cada linha antes de rolar.");
        }
        List<ValorChance> ordenadas = new ArrayList<>(chances.values());
        ordenadas.sort(Comparator.comparingInt(ValorChance::getValorMinimo));
        long proximo = 1;
        for (ValorChance chance : ordenadas) {
            if (chance.getValorMinimo() != proximo) {
                throw new IllegalStateException("As faixas devem cobrir de 1 até o máximo sem lacunas.");
            }
            proximo = (long) chance.getValorMaximo() + 1;
        }
        int resultado = (int) random.nextLong(1, proximo);
        return resolverResultado(resultado);
    }
}
