package com.equipe.shieldman.domain.model;

import java.util.Objects;
import java.util.UUID;

/** Organização de informações de uma campanha ou sessão. */
public final class Escudo {
    private final UUID id;
    private final UUID mestreId;
    private String nome;
    private final NavInfo navInfo = new NavInfo();
    private final Painel painel = new Painel(navInfo);

    Escudo(UUID mestreId, String nome) {
        this(UUID.randomUUID(), mestreId, nome);
    }

    private Escudo(UUID id, UUID mestreId, String nome) {
        this.id = Objects.requireNonNull(id);
        this.mestreId = Objects.requireNonNull(mestreId);
        renomear(nome);
    }

    public static Escudo reconstituir(
            UUID id, UUID mestreId, String nome) {
        return new Escudo(id, mestreId, nome);
    }
    public UUID getId() {
        return id;
    }

    public UUID getMestreId() {
        return mestreId;
    }

    public String getNome() {
        return nome;
    }

    public void renomear(String nome) {
        this.nome = Validacao.textoObrigatorio(nome, "Nome do escudo");
    }

    public Painel getPainel() {
        return painel;
    }

    public NavInfo getNavInfo() {
        return navInfo;
    }
}
