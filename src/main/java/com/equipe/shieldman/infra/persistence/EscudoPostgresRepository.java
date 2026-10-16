package com.equipe.shieldman.infra.persistence;

import com.equipe.shieldman.domain.model.Escudo;
import com.equipe.shieldman.application.Entrada;
import com.equipe.shieldman.domain.repository.EscudoRepository;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class EscudoPostgresRepository implements EscudoRepository, PanacheRepositoryBase<EscudoJpa, UUID> {
    @Override
    @Transactional
    public Escudo salvar(Escudo escudo) {
        Objects.requireNonNull(escudo);
        MestreJpa mestre = getEntityManager().find(MestreJpa.class, escudo.getMestreId());
        if (mestre == null) {
            throw new IllegalArgumentException("Cadastre o mestre antes de salvar seu escudo.");
        }
        EscudoJpa entidade = findById(escudo.getId());
        boolean novo = entidade == null;
        if (novo) {
            entidade = new EscudoJpa();
        } else if (!entidade.mestre.id.equals(escudo.getMestreId())) {
            throw new IllegalArgumentException("Não é permitido trocar o dono de um escudo existente.");
        }
        PersistenciaMapper.atualizarEntidade(escudo, mestre, entidade);
        if (novo) {
            persist(entidade);
        }
        return PersistenciaMapper.paraDominio(entidade);
    }

    @Override
    @Transactional
    public Optional<Escudo> buscarPorId(UUID id) {
        return findByIdOptional(Objects.requireNonNull(id)).map(PersistenciaMapper::paraDominio);
    }

    @Override
    @Transactional
    public List<Escudo> listarPorMestre(UUID mestreId) {
        return find("mestre.id = ?1 order by nome, id", Objects.requireNonNull(mestreId)).list().stream()
                .map(PersistenciaMapper::paraDominio).toList();
    }

    @Override
    @Transactional
    public List<Escudo> listar(UUID mestreId, String nome, int pagina, int tamanho) {
        Entrada.paginacao(pagina, tamanho);
        var consulta = mestreId == null
                ? find("lower(nome) like ?1 escape '!' order by nome, id", ConsultaNome.padrao(nome))
                : find("mestre.id = ?1 and lower(nome) like ?2 escape '!' order by nome, id",
                        mestreId, ConsultaNome.padrao(nome));
        return consulta.page(pagina, tamanho).list().stream().map(PersistenciaMapper::paraDominio).toList();
    }

    @Override
    @Transactional
    public boolean excluir(UUID id) {
        return deleteById(Objects.requireNonNull(id));
    }
}
