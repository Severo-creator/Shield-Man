package com.equipe.shieldman.application;

import com.equipe.shieldman.domain.model.Escudo;
import com.equipe.shieldman.domain.model.Mestre;
import com.equipe.shieldman.domain.repository.EscudoRepository;
import com.equipe.shieldman.domain.repository.MestreRepository;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class GerenciarEscudos {
    private final EscudoRepository escudos;
    private final MestreRepository mestres;

    public GerenciarEscudos(EscudoRepository escudos, MestreRepository mestres) {
        this.escudos = Objects.requireNonNull(escudos);
        this.mestres = Objects.requireNonNull(mestres);
    }

    public Escudo criar(UUID mestreId, String nome) {
        String validado = Entrada.nome(nome, 160);
        Mestre mestre = mestres.buscarPorId(Entrada.id(mestreId))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Mestre não encontrado."));
        return escudos.salvar(mestre.criarEscudo(validado));
    }

    public Escudo buscar(UUID id) {
        return escudos.buscarPorId(Entrada.id(id))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Escudo não encontrado."));
    }

    public List<Escudo> listar(UUID mestreId, String nome, int pagina, int tamanho) {
        Entrada.paginacao(pagina, tamanho);
        return escudos.listar(mestreId, Entrada.filtro(nome), pagina, tamanho);
    }

    public Escudo atualizar(UUID id, String nome) {
        String validado = Entrada.nome(nome, 160);
        Escudo escudo = buscar(id);
        escudo.renomear(validado);
        return escudos.salvar(escudo);
    }

    public void excluir(UUID id) {
        if (!escudos.excluir(Entrada.id(id))) {
            throw new RecursoNaoEncontradoException("Escudo não encontrado.");
        }
    }
}
