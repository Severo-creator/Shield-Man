package com.equipe.shieldman.domain.repository;

import com.equipe.shieldman.domain.model.Mestre;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MestreRepository {
    Mestre salvar(Mestre mestre);

    Optional<Mestre> buscarPorId(UUID id);

    List<Mestre> listar();

    List<Mestre> listar(String nome, int pagina, int tamanho);

    boolean excluir(UUID id);
}
