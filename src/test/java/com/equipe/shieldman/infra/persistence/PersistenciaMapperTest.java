package com.equipe.shieldman.infra.persistence;

import com.equipe.shieldman.domain.model.Escudo;
import com.equipe.shieldman.domain.model.Mestre;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PersistenciaMapperTest {
    @Test
    void mestreRecuperadoPreservaIdNomeEVinculoDosEscudos() {
        Mestre mestre = new Mestre("Mestre");
        Escudo escudo = mestre.criarEscudo("Campanha");
        MestreJpa entidade = new MestreJpa();
        PersistenciaMapper.atualizarEntidade(mestre, entidade);

        Mestre recuperado = PersistenciaMapper.paraDominio(entidade, List.of(escudo));
        assertEquals(mestre.getId(), recuperado.getId());
        assertEquals(mestre.getNome(), recuperado.getNome());
        assertEquals(escudo.getId(), recuperado.buscarEscudo(escudo.getId()).orElseThrow().getId());
        mestre.renomear("Novo nome");
        PersistenciaMapper.atualizarEntidade(mestre, entidade);
        assertEquals("Novo nome", entidade.nome);
    }

    @Test
    void recuperacaoDoMestreRejeitaEscudosDeOutroDonoEDuplicados() {
        Mestre mestre = new Mestre("Mestre");
        Escudo escudo = mestre.criarEscudo("Campanha");
        Escudo outro = new Mestre("Outro").criarEscudo("Outra campanha");
        assertThrows(IllegalArgumentException.class,
                () -> Mestre.reconstituir(mestre.getId(), mestre.getNome(), List.of(outro)));
        assertThrows(IllegalArgumentException.class,
                () -> Mestre.reconstituir(mestre.getId(), mestre.getNome(), List.of(escudo, escudo)));
    }

    @Test
    void conversaoPreservaIdentificadoresENome() {
        MestreJpa mestre = new MestreJpa();
        mestre.id = UUID.randomUUID();
        Escudo escudo = Escudo.reconstituir(UUID.randomUUID(), mestre.id, "Campanha");
        EscudoJpa entidade = new EscudoJpa();
        PersistenciaMapper.atualizarEntidade(escudo, mestre, entidade);

        Escudo recuperado = PersistenciaMapper.paraDominio(entidade);
        assertEquals(escudo.getId(), recuperado.getId());
        assertEquals(mestre.id, recuperado.getMestreId());
        assertEquals("Campanha", recuperado.getNome());

        escudo.renomear("Novo nome");
        PersistenciaMapper.atualizarEntidade(escudo, mestre, entidade);
        assertEquals("Novo nome", entidade.nome);
    }

    @Test
    void conversaoRejeitaOutroMestreAntesDeModificarEntidade() {
        MestreJpa mestre = new MestreJpa();
        mestre.id = UUID.randomUUID();
        Escudo escudo = Escudo.reconstituir(UUID.randomUUID(), UUID.randomUUID(), "Campanha");
        EscudoJpa entidade = new EscudoJpa();
        assertThrows(IllegalArgumentException.class,
                () -> PersistenciaMapper.atualizarEntidade(escudo, mestre, entidade));
        assertNull(entidade.id);
        assertNull(entidade.nome);
    }
}
