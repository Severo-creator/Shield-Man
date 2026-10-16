package com.equipe.shieldman.infra.persistence;

import jakarta.persistence.*;

import java.util.UUID;


@Entity
@Table(name = "escudo")
public class EscudoJpa {
    @Id
    public UUID id;

    @Column(nullable = false, length = 160)
    public String nome;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mestre_id", nullable = false)
    public MestreJpa mestre;
}
