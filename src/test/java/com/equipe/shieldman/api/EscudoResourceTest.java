package com.equipe.shieldman.api;

import com.equipe.shieldman.application.ConsultarEscudo;
import com.equipe.shieldman.application.GerenciarEscudos;
import com.equipe.shieldman.application.RecursoNaoEncontradoException;
import com.equipe.shieldman.infra.memory.BancoEmMemoria;
import com.equipe.shieldman.infra.memory.EscudoEmMemoriaRepository;
import com.equipe.shieldman.infra.memory.MestreEmMemoriaRepository;
import com.equipe.shieldman.domain.model.Escudo;
import com.equipe.shieldman.domain.model.Mestre;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EscudoResourceTest {
    @Test
    void rotaRetornaDadosDoCasoDeUsoOu404SemDependerDePostgres() {
        BancoEmMemoria banco = new BancoEmMemoria();
        var mestres = new MestreEmMemoriaRepository(banco);
        var escudos = new EscudoEmMemoriaRepository(banco);
        Mestre mestre = new Mestre("Mestre");
        mestres.salvar(mestre);
        Escudo escudo = mestre.criarEscudo("Campanha");
        escudos.salvar(escudo);
        EscudoResource rota = new EscudoResource(new ConsultarEscudo(escudos),
                new GerenciarEscudos(escudos, mestres));

        try (Response resposta = rota.buscarPorId(escudo.getId().toString())) {
            assertEquals(200, resposta.getStatus());
            var dados = (EscudoResource.EscudoResposta) resposta.getEntity();
            assertEquals(escudo.getId(), dados.id());
            assertEquals(mestre.getId(), dados.mestreId());
            assertEquals("Campanha", dados.nome());
        }
        assertThrows(RecursoNaoEncontradoException.class,
                () -> rota.buscarPorId(UUID.randomUUID().toString()));
        assertThrows(IllegalArgumentException.class, () -> rota.buscarPorId("UUID_DO_ESCUDO"));
    }
}
