package com.equipe.shieldman.infra.persistence;

import com.equipe.shieldman.application.Entrada;

final class ConsultaNome {
    private ConsultaNome() { }

    static String padrao(String nome) {
        // O filtro é literal: % e _ informados pelo usuário não viram curingas SQL.
        return "%" + Entrada.filtro(nome).replace("!", "!!").replace("%", "!%")
                .replace("_", "!_") + "%";
    }
}
