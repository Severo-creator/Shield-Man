package com.equipe.shieldman.domain.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.random.RandomGenerator;

/** Combina nomes e sobrenomes cadastrados manualmente. */
public final class BancoNomes extends Infograma {
    private final List<String> nomes = new ArrayList<>();
    private final List<String> sobrenomes = new ArrayList<>();

    public BancoNomes(String titulo) { super(titulo); }

    public List<String> getNomes() { return List.copyOf(nomes); }
    public List<String> getSobrenomes() { return List.copyOf(sobrenomes); }

    public void adicionarNome(String nome) {
        nomes.add(Validacao.textoObrigatorio(nome, "Nome"));
    }

    public void adicionarSobrenome(String sobrenome) {
        sobrenomes.add(Validacao.textoObrigatorio(sobrenome, "Sobrenome"));
    }

    public String gerarNome() { return gerarNome(RandomGenerator.getDefault()); }

    public String gerarNome(RandomGenerator random) {
        Objects.requireNonNull(random);
        if (nomes.isEmpty()) {
            throw new IllegalStateException("Cadastre pelo menos um nome antes de sortear.");
        }
        String nome = nomes.get(random.nextInt(nomes.size()));
        return sobrenomes.isEmpty() ? nome : nome + " " + sobrenomes.get(random.nextInt(sobrenomes.size()));
    }
}
