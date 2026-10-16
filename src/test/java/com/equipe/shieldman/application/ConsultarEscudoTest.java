package com.equipe.shieldman.application;

import com.equipe.shieldman.infra.memory.BancoEmMemoria;
import com.equipe.shieldman.infra.memory.EscudoEmMemoriaRepository;
import com.equipe.shieldman.infra.memory.MestreEmMemoriaRepository;
import com.equipe.shieldman.domain.model.Escudo;
import com.equipe.shieldman.domain.model.Mestre;
import com.equipe.shieldman.domain.repository.EscudoRepository;
import com.equipe.shieldman.domain.repository.MestreRepository;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ConsultarEscudoTest {
    @Test
    void mesmoCasoDeUsoFuncionaComImplementacaoEmMemoriaSemIniciarQuarkus() {
        BancoEmMemoria banco = new BancoEmMemoria();
        MestreRepository mestres = new MestreEmMemoriaRepository(banco);
        EscudoRepository escudos = new EscudoEmMemoriaRepository(banco);
        Mestre mestre = new Mestre("Guilherme");
        mestres.salvar(mestre);
        Escudo escudo = mestre.criarEscudo("Minha campanha");
        escudos.salvar(escudo);

        ConsultarEscudo consultar = new ConsultarEscudo(escudos);
        assertEquals("Minha campanha", consultar.executar(escudo.getId()).orElseThrow().getNome());
        assertTrue(consultar.executar(UUID.randomUUID()).isEmpty());
        escudos.excluir(escudo.getId());
        assertTrue(consultar.executar(escudo.getId()).isEmpty());
    }
}
