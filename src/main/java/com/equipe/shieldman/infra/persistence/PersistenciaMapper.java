package com.equipe.shieldman.infra.persistence;

import com.equipe.shieldman.domain.model.Escudo;
import com.equipe.shieldman.domain.model.Mestre;
import java.util.List;
import java.util.Objects;

/** Conversões entre os dados persistidos e o domínio, sem copiar o painel em memória. */
public final class PersistenciaMapper {
    private PersistenciaMapper() {
    }

    public static Escudo paraDominio(EscudoJpa entidade) {
        Objects.requireNonNull(entidade);
        return Escudo.reconstituir(entidade.id, entidade.mestre.id, entidade.nome);
    }

    public static Mestre paraDominio(MestreJpa entidade, List<Escudo> escudos) {
        Objects.requireNonNull(entidade);
        return Mestre.reconstituir(entidade.id, entidade.nome, escudos);
    }

    public static void atualizarEntidade(Mestre mestre, MestreJpa entidade) {
        Objects.requireNonNull(mestre);
        Objects.requireNonNull(entidade);
        entidade.id = mestre.getId();
        entidade.nome = mestre.getNome();
    }

    public static void atualizarEntidade(Escudo escudo, MestreJpa mestre, EscudoJpa entidade) {
        Objects.requireNonNull(escudo);
        Objects.requireNonNull(mestre);
        Objects.requireNonNull(entidade);
        if (!escudo.getMestreId().equals(mestre.id)) {
            throw new IllegalArgumentException("Mestre informado não é o dono do escudo.");
        }
        entidade.id = escudo.getId();
        entidade.nome = escudo.getNome();
        entidade.mestre = mestre;
    }
}

