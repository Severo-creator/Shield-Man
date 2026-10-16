package com.equipe.shieldman.infra.persistence;

import com.equipe.shieldman.domain.model.Mestre;
import com.equipe.shieldman.application.ConflitoException;
import com.equipe.shieldman.application.Entrada;
import com.equipe.shieldman.domain.repository.EscudoRepository;
import com.equipe.shieldman.domain.repository.MestreRepository;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** Persiste o cadastro do mestre; os escudos são salvos pelo EscudoRepository. */
@ApplicationScoped
public class MestrePostgresRepository implements MestreRepository, PanacheRepositoryBase<MestreJpa, UUID> {
    @Inject
    EscudoRepository escudos;

    @Override
    @Transactional
    public Mestre salvar(Mestre mestre) {
        Objects.requireNonNull(mestre);
        MestreJpa entidade = findById(mestre.getId());
        boolean novo = entidade == null;
        if (novo) {
            entidade = new MestreJpa();
        }
        PersistenciaMapper.atualizarEntidade(mestre, entidade);
        if (novo) {
            persist(entidade);
        }
        return carregarMestre(entidade);
    }

    @Override
    @Transactional
    public Optional<Mestre> buscarPorId(UUID id) {
        return findByIdOptional(Objects.requireNonNull(id)).map(this::carregarMestre);
    }

    @Override
    @Transactional
    public List<Mestre> listar() {
        return find("order by nome, id").list().stream().map(this::carregarMestre).toList();
    }

    @Override
    @Transactional
    public List<Mestre> listar(String nome, int pagina, int tamanho) {
        Entrada.paginacao(pagina, tamanho);
        return find("lower(nome) like ?1 escape '!' order by nome, id", ConsultaNome.padrao(nome))
                .page(pagina, tamanho).list().stream().map(this::carregarMestre).toList();
    }

    @Override
    @Transactional
    public boolean excluir(UUID id) {
        Objects.requireNonNull(id);
        if (findById(id) == null) {
            return false;
        }
        long quantidadeEscudos = getEntityManager().createQuery(
                "select count(e) from EscudoJpa e where e.mestre.id = :id", Long.class)
                .setParameter("id", id).getSingleResult();
        if (quantidadeEscudos > 0) {
            throw new ConflitoException("Remova os escudos antes de excluir o mestre.");
        }
        return deleteById(id);
    }

    private Mestre carregarMestre(MestreJpa entidade) {
        return PersistenciaMapper.paraDominio(entidade, escudos.listarPorMestre(entidade.id));
    }
}
