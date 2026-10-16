package com.equipe.shieldman.domain.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** Barra direita: mantém os infogramas que os contêineres consultam ao serem criados. */
public final class NavInfo {
    private final List<Infograma> infogramas = new ArrayList<>();

    NavInfo() {
    }

    public void adicionarInfograma(Infograma infograma) {
        Objects.requireNonNull(infograma, "Infograma deve ser informado.");
        if (buscarInfograma(infograma.getId()).isPresent()) {
            throw new IllegalArgumentException("Infograma já está neste NavInfo.");
        }
        infogramas.add(infograma);
    }

    public List<Infograma> listarInfogramas() {
        return List.copyOf(infogramas);
    }

    public Optional<Infograma> buscarInfograma(UUID infogramaId) {
        Objects.requireNonNull(infogramaId, "Identificador do infograma deve ser informado.");
        return infogramas.stream()
                .filter(infograma -> infograma.getId().equals(infogramaId)).findFirst();
    }
}
