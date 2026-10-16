package com.equipe.shieldman.domain.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/** Centraliza alterações para manter as linhas e colunas com as mesmas células. */
public class Tabela extends Infograma {
    private final List<Linha> linhas = new ArrayList<>();
    private final List<Coluna> colunas = new ArrayList<>();

    public Tabela(String titulo) { super(titulo); }

    public List<Linha> getLinhas() { return List.copyOf(linhas); }
    public List<Coluna> getColunas() { return List.copyOf(colunas); }

    public Coluna adicionarColuna(String titulo) {
        Coluna coluna = new Coluna(titulo, colunas.size());
        for (Linha linha : linhas) {
            ValorCampo campo = new ValorCampo(linha.getId(), coluna.getId(), "");
            linha.adicionarCampo(campo);
            coluna.adicionarCampo(campo);
        }
        colunas.add(coluna);
        return coluna;
    }

    public Linha adicionarLinha(List<String> valores) {
        List<String> copia = List.copyOf(Objects.requireNonNull(valores));
        if (copia.size() != colunas.size()) {
            throw new IllegalArgumentException("Informe um valor para cada coluna da tabela.");
        }
        Linha linha = new Linha(linhas.size());
        for (int i = 0; i < colunas.size(); i++) {
            Coluna coluna = colunas.get(i);
            ValorCampo campo = new ValorCampo(linha.getId(), coluna.getId(), copia.get(i));
            linha.adicionarCampo(campo);
            coluna.adicionarCampo(campo);
        }
        linhas.add(linha);
        return linha;
    }

    public Linha buscarLinha(UUID linhaId) {
        return linhas.stream().filter(linha -> linha.getId().equals(linhaId)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Linha não pertence a esta tabela."));
    }

    public void editarCampo(UUID linhaId, UUID colunaId, String texto) {
        ValorCampo campo = buscarLinha(linhaId).getCampos().stream()
                .filter(item -> item.getColunaId().equals(colunaId)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Coluna não pertence a esta tabela."));
        campo.editar(texto);
    }

    public String gerarTabelaTexto() {
        String cabecalho = colunas.stream().map(Coluna::getTitulo).collect(Collectors.joining(" | "));
        String corpo = linhas.stream().map(Linha::gerarTexto).collect(Collectors.joining("\n"));
        return corpo.isEmpty() ? cabecalho : cabecalho + "\n" + corpo;
    }
}
