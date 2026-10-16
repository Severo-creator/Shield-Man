package com.equipe.shieldman.domain.repository;

import com.equipe.shieldman.domain.model.Escudo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EscudoRepository {
    Escudo salvar(Escudo escudo);

    Optional<Escudo> buscarPorId(UUID id);

    List<Escudo> listarPorMestre(UUID mestreId);

    List<Escudo> listar(UUID mestreId, String nome, int pagina, int tamanho);

    boolean excluir(UUID id);
}
