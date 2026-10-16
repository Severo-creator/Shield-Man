package com.equipe.shieldman.application;

import com.equipe.shieldman.infra.memory.BancoEmMemoria;
import com.equipe.shieldman.infra.memory.EscudoEmMemoriaRepository;
import com.equipe.shieldman.infra.memory.MestreEmMemoriaRepository;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class GerenciarCadastrosTest {
    private final BancoEmMemoria banco = new BancoEmMemoria();
    private final MestreEmMemoriaRepository repositorioMestres = new MestreEmMemoriaRepository(banco);
    private final EscudoEmMemoriaRepository repositorioEscudos = new EscudoEmMemoriaRepository(banco);
    private final GerenciarMestres mestres = new GerenciarMestres(repositorioMestres);
    private final GerenciarEscudos escudos = new GerenciarEscudos(repositorioEscudos, repositorioMestres);

    @Test
    void cicloCompletoPreservaVinculoEValidaExclusao() {
        var mestre = mestres.criar(" Guilherme ");
        var escudo = escudos.criar(mestre.getId(), "Campanha");
        assertEquals("Guilherme", mestres.buscar(mestre.getId()).getNome());
        assertEquals(escudo.getId(), mestres.buscar(mestre.getId()).getEscudos().getFirst().getId());
        assertEquals("Novo nome", mestres.atualizar(mestre.getId(), "Novo nome").getNome());
        assertEquals("Nova campanha", escudos.atualizar(escudo.getId(), "Nova campanha").getNome());
        assertThrows(ConflitoException.class, () -> mestres.excluir(mestre.getId()));
        escudos.excluir(escudo.getId());
        mestres.excluir(mestre.getId());
        assertThrows(RecursoNaoEncontradoException.class, () -> escudos.buscar(escudo.getId()));
        assertThrows(RecursoNaoEncontradoException.class, () -> mestres.buscar(mestre.getId()));
        assertThrows(RecursoNaoEncontradoException.class, () -> escudos.excluir(escudo.getId()));
    }

    @Test
    void validacaoImpedeGravacoesInvalidas() {
        assertThrows(IllegalArgumentException.class, () -> mestres.criar(null));
        assertThrows(IllegalArgumentException.class, () -> mestres.criar(" "));
        assertThrows(IllegalArgumentException.class, () -> mestres.criar("a".repeat(121)));
        var mestre = mestres.criar("Mestre");
        assertThrows(IllegalArgumentException.class, () -> escudos.criar(mestre.getId(), "a".repeat(161)));
        assertThrows(IllegalArgumentException.class, () -> escudos.criar(null, "Campanha"));
        assertThrows(RecursoNaoEncontradoException.class, () -> escudos.criar(UUID.randomUUID(), "Campanha"));
        assertTrue(escudos.listar(null, null, 0, 20).isEmpty());
        assertEquals("Mestre", mestres.buscar(mestre.getId()).getNome());
    }

    @Test
    void paginacaoEFiltroRespeitamLimitesEDono() {
        var mestre = mestres.criar("Guilherme");
        var outro = mestres.criar("Outro mestre");
        escudos.criar(mestre.getId(), "B 100%_");
        escudos.criar(mestre.getId(), "A campanha");
        escudos.criar(outro.getId(), "Outra campanha");
        assertEquals("A campanha", escudos.listar(mestre.getId(), null, 0, 1).getFirst().getNome());
        assertEquals("B 100%_", escudos.listar(mestre.getId(), null, 1, 1).getFirst().getNome());
        assertEquals(1, escudos.listar(null, "100%_", 0, 20).size());
        assertEquals(1, mestres.listar("GUILHERME", 0, 20).size());
        assertTrue(escudos.listar(null, null, 10, 20).isEmpty());
        assertThrows(IllegalArgumentException.class, () -> mestres.listar(null, -1, 20));
        assertThrows(IllegalArgumentException.class, () -> escudos.listar(null, null, 0, 101));
        assertThrows(IllegalArgumentException.class, () -> escudos.listar(null, null, Integer.MAX_VALUE, 100));
    }
}
