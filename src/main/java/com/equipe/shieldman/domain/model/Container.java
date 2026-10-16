package com.equipe.shieldman.domain.model;

import java.util.Objects;
import java.util.UUID;

/** Bloco visual: guarda posição e tamanho, enquanto o infograma guarda o conteúdo. */
public final class Container {
    private final UUID id = UUID.randomUUID();
    private final UUID painelId;
    private final Infograma infograma;
    private double x;
    private double y;
    private double largura = 240;
    private double altura = 160;

    Container(UUID painelId, NavInfo navInfo, UUID infogramaId, double x, double y) {
        this.painelId = Objects.requireNonNull(painelId);
        this.infograma = Objects.requireNonNull(navInfo).buscarInfograma(infogramaId)
                .orElseThrow(() -> new IllegalArgumentException("Infograma não pertence ao NavInfo deste escudo."));
        moverPara(x, y);
    }

    public UUID getId() {
        return id;
    }

    public UUID getPainelId() {
        return painelId;
    }

    public Infograma getInfograma() {
        return infograma;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getLargura() {
        return largura;
    }

    public double getAltura() {
        return altura;
    }

    public void moverPara(double x, double y) {
        // Valida os dois valores antes de modificar o estado.
        double novoX = Validacao.coordenada(x, "X");
        double novoY = Validacao.coordenada(y, "Y");
        this.x = novoX;
        this.y = novoY;
    }

    public void redimensionar(double largura, double altura) {
        double novaLargura = Validacao.dimensao(largura, "Largura");
        double novaAltura = Validacao.dimensao(altura, "Altura");
        this.largura = novaLargura;
        this.altura = novaAltura;
    }
}
