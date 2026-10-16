package com.equipe.shieldman.domain.model;

final class Validacao {
    private Validacao() {
    }

    static String textoObrigatorio(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(campo + " deve ser informado.");
        }
        return valor.strip();
    }

    static double coordenada(double valor, String campo) {
        if (!Double.isFinite(valor) || valor < 0) {
            throw new IllegalArgumentException(campo + " deve ser finito e maior ou igual a zero.");
        }
        return valor;
    }

    static double dimensao(double valor, String campo) {
        if (!Double.isFinite(valor) || valor <= 0) {
            throw new IllegalArgumentException(campo + " deve ser finito e maior que zero.");
        }
        return valor;
    }
}
