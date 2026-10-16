package com.equipe.shieldman.domain.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** Usuário que organiza os escudos exibidos na barra esquerda. */
public final class Mestre {
    private final UUID id;
    private String nome;
    private final List<Escudo> escudos = new ArrayList<>();

    public Mestre(String nome) {
        this(UUID.randomUUID(), nome);
    }

    private Mestre(UUID id, String nome) {
        this.id = Objects.requireNonNull(id);
        renomear(nome);
    }

    public static Mestre reconstituir(UUID id, String nome, List<Escudo> escudos) {
        Mestre mestre = new Mestre(id, nome);
        for (Escudo escudo : List.copyOf(escudos)) {
            if (!id.equals(escudo.getMestreId())) {
                throw new IllegalArgumentException("Escudo não pertence a este mestre.");
            }
            if (mestre.buscarEscudo(escudo.getId()).isPresent()) {
                throw new IllegalArgumentException("Escudo duplicado na recuperação do mestre.");
            }
            mestre.escudos.add(escudo);
        }
        return mestre;
    }

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void renomear(String nome) {
        this.nome = Validacao.textoObrigatorio(nome, "Nome do mestre");
    }

    public List<Escudo> getEscudos() {
        return List.copyOf(escudos);
    }

    public Escudo criarEscudo(String nome) {
        Escudo escudo = new Escudo(id, nome);
        escudos.add(escudo);
        return escudo;
    }

    public Optional<Escudo> buscarEscudo(UUID escudoId) {
        return escudos.stream().filter(escudo -> escudo.getId().equals(escudoId)).findFirst();
    }

    public void removerEscudo(UUID escudoId) {
        Escudo escudo = buscarEscudo(escudoId)
                .orElseThrow(() -> new IllegalArgumentException("Escudo não pertence a este mestre."));
        escudos.remove(escudo);
    }
}
