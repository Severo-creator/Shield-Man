package com.equipe.shieldman.infra.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "mestre")
public class MestreJpa {
    @Id
    public UUID id;

    @Column(nullable = false, length = 120)
    public String nome;
}