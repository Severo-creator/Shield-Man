package com.equipe.shieldman.infra.memory;

import com.equipe.shieldman.domain.model.Escudo;
import com.equipe.shieldman.application.Entrada;
import com.equipe.shieldman.domain.repository.EscudoRepository;

import java.util.List;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class EscudoEmMemoriaRepository implements EscudoRepository {
    private final BancoEmMemoria banco;

    public EscudoEmMemoriaRepository(BancoEmMemoria banco) {
        this.banco = Objects.requireNonNull(banco);
    }

    @Override
    public Escudo salvar(Escudo escudo) {
        Objects.requireNonNull(escudo);
        if (!banco.mestres.containsKey(escudo.getMestreId())) {
            throw new IllegalArgumentException("Cadastre o mestre antes de salvar seu escudo.");
        }
        Escudo existente = banco.escudos.get(escudo.getId());
        if (existente != null && !existente.getMestreId().equals(escudo.getMestreId())) {
            throw new IllegalArgumentException("Não é permitido trocar o dono de um escudo existente.");
        }
        if (escudo.getNome().length() > 160) {
            throw new IllegalArgumentException("Nome do escudo deve ter até 160 caracteres.");
        }
        banco.escudos.put(escudo.getId(), BancoEmMemoria.copiar(escudo));
        return BancoEmMemoria.copiar(escudo);
    }

    @Override
    public Optional<Escudo> buscarPorId(UUID id) {
        return Optional.ofNullable(banco.escudos.get(Objects.requireNonNull(id)))
                .map(BancoEmMemoria::copiar);
    }

    @Override
    public List<Escudo> listarPorMestre(UUID mestreId) {
        return banco.escudosDoMestre(Objects.requireNonNull(mestreId));
    }

    @Override
    public List<Escudo> listar(UUID mestreId, String nome, int pagina, int tamanho) {
        Entrada.paginacao(pagina, tamanho);
        String filtro = Entrada.filtro(nome);
        return banco.escudos.values().stream()
                .filter(escudo -> mestreId == null || escudo.getMestreId().equals(mestreId))
                .filter(escudo -> Entrada.filtro(escudo.getNome()).contains(filtro))
                .sorted(Comparator.comparing(Escudo::getNome).thenComparing(Escudo::getId))
                .skip((long) pagina * tamanho).limit(tamanho).map(BancoEmMemoria::copiar).toList();
    }

    @Override
    public boolean excluir(UUID id) {
        return banco.escudos.remove(Objects.requireNonNull(id)) != null;
    }
}
