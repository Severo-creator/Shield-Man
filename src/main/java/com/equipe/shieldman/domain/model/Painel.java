package com.equipe.shieldman.domain.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** Área central do escudo, que contém e organiza os contêineres. */
public final class Painel {
    private final UUID id = UUID.randomUUID();
    private final NavInfo navInfo;
    private final List<Container> containers = new ArrayList<>();

    Painel(NavInfo navInfo) {
        this.navInfo = Objects.requireNonNull(navInfo);
    }

    public UUID getId() {
        return id;
    }

    public List<Container> getContainers() {
        return List.copyOf(containers);
    }

    public Container adicionarContainer(UUID infogramaId, double x, double y) {
        boolean jaAdicionado = containers.stream()
                .anyMatch(container -> container.getInfograma().getId().equals(infogramaId));
        if (jaAdicionado) {
            throw new IllegalArgumentException("Infograma já está neste painel.");
        }
        Container container = new Container(id, navInfo, infogramaId, x, y);
        containers.add(container);
        return container;
    }

    public Optional<Container> buscarContainer(UUID containerId) {
        return containers.stream().filter(container -> container.getId().equals(containerId)).findFirst();
    }

    public void removerContainer(UUID containerId) {
        Container container = buscarContainer(containerId)
                .orElseThrow(() -> new IllegalArgumentException("Contêiner não pertence a este painel."));
        containers.remove(container);
    }
}
