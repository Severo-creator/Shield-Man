package com.equipe.shieldman.application;

import com.equipe.shieldman.domain.model.Mestre;
import com.equipe.shieldman.domain.repository.MestreRepository;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class GerenciarMestres {
    private final MestreRepository mestres;

    public GerenciarMestres(MestreRepository mestres) { this.mestres = Objects.requireNonNull(mestres); }

    public Mestre criar(String nome) { return mestres.salvar(new Mestre(Entrada.nome(nome, 120))); }

    public Mestre buscar(UUID id) {
        return mestres.buscarPorId(Entrada.id(id))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Mestre não encontrado."));
    }

    public List<Mestre> listar(String nome, int pagina, int tamanho) {
        Entrada.paginacao(pagina, tamanho);
        return mestres.listar(Entrada.filtro(nome), pagina, tamanho);
    }

    public Mestre atualizar(UUID id, String nome) {
        String validado = Entrada.nome(nome, 120);
        Mestre mestre = buscar(id);
        mestre.renomear(validado);
        return mestres.salvar(mestre);
    }

    public void excluir(UUID id) {
        Mestre mestre = buscar(id);
        if (!mestre.getEscudos().isEmpty()) {
            throw new ConflitoException("Remova os escudos antes de excluir o mestre.");
        }
        if (!mestres.excluir(id)) {
            throw new RecursoNaoEncontradoException("Mestre não encontrado.");
        }
    }
}
