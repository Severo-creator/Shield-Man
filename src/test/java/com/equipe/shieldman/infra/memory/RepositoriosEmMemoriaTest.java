package com.equipe.shieldman.infra.memory;

import com.equipe.shieldman.domain.model.Escudo;
import com.equipe.shieldman.domain.model.Mestre;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RepositoriosEmMemoriaTest {
    private final BancoEmMemoria banco = new BancoEmMemoria();
    private final MestreEmMemoriaRepository mestres = new MestreEmMemoriaRepository(banco);
    private final EscudoEmMemoriaRepository escudos = new EscudoEmMemoriaRepository(banco);

    @Test
    void alteracoesNoObjetoOriginalNoRetornoENaConsultaExigemSalvarNovamente() {
        Mestre mestre = new Mestre("Guilherme");
        Mestre retornoMestre = mestres.salvar(mestre);
        mestre.renomear("Alterado fora do repositório");
        retornoMestre.renomear("Alterado no retorno");
        assertEquals("Guilherme", mestres.buscarPorId(mestre.getId()).orElseThrow().getNome());

        Escudo escudo = mestre.criarEscudo("Campanha");
        Escudo retorno = escudos.salvar(escudo);
        escudo.renomear("Alteração original");
        retorno.renomear("Alteração do retorno");
        Escudo consultado = escudos.buscarPorId(escudo.getId()).orElseThrow();
        assertEquals("Campanha", consultado.getNome());
        consultado.renomear("Novo nome");
        assertEquals("Campanha", escudos.buscarPorId(escudo.getId()).orElseThrow().getNome());
        escudos.salvar(consultado);
        assertEquals("Novo nome", escudos.buscarPorId(escudo.getId()).orElseThrow().getNome());
        mestres.listar().getFirst().renomear("Alteração na listagem");
        assertEquals("Guilherme", mestres.buscarPorId(mestre.getId()).orElseThrow().getNome());
    }

    @Test
    void vinculoExigeMestreExistenteEImpedeTrocaDeDono() {
        Mestre mestre = new Mestre("Mestre");
        Escudo escudo = mestre.criarEscudo("Campanha");
        assertThrows(IllegalArgumentException.class, () -> escudos.salvar(escudo));
        mestres.salvar(mestre);
        escudos.salvar(escudo);
        Mestre outro = new Mestre("Outro");
        mestres.salvar(outro);
        Escudo troca = Escudo.reconstituir(escudo.getId(), outro.getId(), "Troca");
        assertThrows(IllegalArgumentException.class, () -> escudos.salvar(troca));
        assertEquals(mestre.getId(), escudos.buscarPorId(escudo.getId()).orElseThrow().getMestreId());
    }

    @Test
    void mestreRecuperaApenasEscudosPersistidosEExigeRemocaoAntesDeExcluir() {
        Mestre mestre = new Mestre("Mestre");
        Escudo escudo = mestre.criarEscudo("Campanha");
        assertTrue(mestres.salvar(mestre).getEscudos().isEmpty());
        escudos.salvar(escudo);
        Mestre recuperado = mestres.buscarPorId(mestre.getId()).orElseThrow();
        assertEquals(escudo.getId(), recuperado.getEscudos().getFirst().getId());
        recuperado.getEscudos().getFirst().renomear("Alterado");
        assertEquals("Campanha", escudos.listarPorMestre(mestre.getId()).getFirst().getNome());
        assertThrows(IllegalStateException.class, () -> mestres.excluir(mestre.getId()));
        assertTrue(escudos.excluir(escudo.getId()));
        assertFalse(escudos.excluir(escudo.getId()));
        assertTrue(mestres.buscarPorId(mestre.getId()).orElseThrow().getEscudos().isEmpty());
        assertTrue(mestres.excluir(mestre.getId()));
        assertFalse(mestres.excluir(mestre.getId()));
        assertTrue(mestres.listar().isEmpty());
        assertTrue(escudos.buscarPorId(UUID.randomUUID()).isEmpty());
    }

    @Test
    void listagemFiltraPorMestreEOrdenaPeloNome() {
        Mestre mestre = new Mestre("Mestre");
        Mestre outro = new Mestre("Outro");
        mestres.salvar(mestre);
        mestres.salvar(outro);
        escudos.salvar(mestre.criarEscudo("Z"));
        escudos.salvar(mestre.criarEscudo("A"));
        escudos.salvar(outro.criarEscudo("Outro escudo"));
        assertEquals(2, escudos.listarPorMestre(mestre.getId()).size());
        assertEquals("A", escudos.listarPorMestre(mestre.getId()).getFirst().getNome());
        assertEquals(1, escudos.listarPorMestre(outro.getId()).size());
    }
}
