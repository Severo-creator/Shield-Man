package com.equipe.shieldman.infra.memory;

import com.equipe.shieldman.domain.model.Mestre;
import com.equipe.shieldman.application.ConflitoException;
import com.equipe.shieldman.application.Entrada;
import com.equipe.shieldman.domain.repository.MestreRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class MestreEmMemoriaRepository implements MestreRepository {
    private final BancoEmMemoria banco;

    public MestreEmMemoriaRepository(BancoEmMemoria banco) {
        this.banco = Objects.requireNonNull(banco);
    }

    @Override
    public Mestre salvar(Mestre mestre) {
        Objects.requireNonNull(mestre);
        if (mestre.getNome().length() > 120) {
            throw new IllegalArgumentException("Nome do mestre deve ter até 120 caracteres.");
        }
        banco.mestres.put(mestre.getId(), Mestre.reconstituir(mestre.getId(), mestre.getNome(), List.of()));
        return banco.copiar(mestre);
    }

    @Override
    public Optional<Mestre> buscarPorId(UUID id) {
        return Optional.ofNullable(banco.mestres.get(Objects.requireNonNull(id))).map(banco::copiar);
    }

    @Override
    public List<Mestre> listar() {
        return banco.mestres.values().stream()
                .sorted(Comparator.comparing(Mestre::getNome).thenComparing(Mestre::getId))
                .map(banco::copiar).toList();
    }

    @Override
    public List<Mestre> listar(String nome, int pagina, int tamanho) {
        Entrada.paginacao(pagina, tamanho);
        String filtro = Entrada.filtro(nome);
        return listar().stream().filter(mestre -> Entrada.filtro(mestre.getNome()).contains(filtro))
                .skip((long) pagina * tamanho).limit(tamanho).toList();
    }

    @Override
    public boolean excluir(UUID id) {
        Objects.requireNonNull(id);
        if (!banco.mestres.containsKey(id)) {
            return false;
        }
        if (banco.escudos.values().stream().anyMatch(escudo -> escudo.getMestreId().equals(id))) {
            throw new ConflitoException("Remova os escudos antes de excluir o mestre.");
        }
        return banco.mestres.remove(id) != null;
    }
}
