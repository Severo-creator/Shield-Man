package com.equipe.shieldman.application;

import com.equipe.shieldman.domain.model.Escudo;
import com.equipe.shieldman.domain.repository.EscudoRepository;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** Caso de uso independente de HTTP, Quarkus e banco de dados. */
public final class ConsultarEscudo {
    private final EscudoRepository repository;

    public ConsultarEscudo(EscudoRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    public Optional<Escudo> executar(UUID id) {
        return repository.buscarPorId(Objects.requireNonNull(id));
    }
}
