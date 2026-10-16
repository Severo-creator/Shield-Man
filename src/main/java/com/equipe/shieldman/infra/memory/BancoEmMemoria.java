package com.equipe.shieldman.infra.memory;

import com.equipe.shieldman.domain.model.Escudo;
import com.equipe.shieldman.domain.model.Mestre;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Estado compartilhado pelos dois repositórios de um teste, sem CDI nem PostgreSQL. */
public final class BancoEmMemoria {
    final Map<UUID, Mestre> mestres = new HashMap<>();
    final Map<UUID, Escudo> escudos = new HashMap<>();

    static Escudo copiar(Escudo escudo) {
        return Escudo.reconstituir(escudo.getId(), escudo.getMestreId(), escudo.getNome());
    }

    List<Escudo> escudosDoMestre(UUID mestreId) {
        return escudos.values().stream().filter(item -> item.getMestreId().equals(mestreId))
                .sorted(Comparator.comparing(Escudo::getNome).thenComparing(Escudo::getId))
                .map(BancoEmMemoria::copiar).toList();
    }

    Mestre copiar(Mestre mestre) {
        return Mestre.reconstituir(mestre.getId(), mestre.getNome(), escudosDoMestre(mestre.getId()));
    }
}
