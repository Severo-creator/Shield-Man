package com.equipe.shieldman;

import com.equipe.shieldman.domain.model.Container;
import com.equipe.shieldman.domain.model.Documento;
import com.equipe.shieldman.domain.model.Escudo;
import com.equipe.shieldman.domain.model.Infograma;
import com.equipe.shieldman.domain.model.Mestre;
import com.equipe.shieldman.domain.model.BancoNomes;
import com.equipe.shieldman.domain.model.Linha;
import com.equipe.shieldman.domain.model.TabelaRolavel;

import java.util.List;

/** Exemplo de uso das classes, sem depender de uma interface ou banco de dados. */
public final class ExemploShieldMan {
    private ExemploShieldMan() {
    }

    public static void main(String[] args) {
        Mestre mestre = new Mestre("Guilherme");
        Escudo escudo = mestre.criarEscudo("Salão do Slomão");
        mestre.criarEscudo("Xileninion");

        Documento notas = new Documento("Resumo da sessão", "Os aventureiros chegaram à taverna.");
        escudo.getNavInfo().adicionarInfograma(notas);
        Container container = escudo.getPainel().adicionarContainer(notas.getId(), 20, 40);
        container.moverPara(80, 120);
        notas.editarTexto("Na taverna, o grupo encontrou uma pista sobre o desaparecimento.");

        System.out.println("Barra esquerda - escudos do mestre:");
        mestre.getEscudos().forEach(item -> System.out.println("  " + item.getNome()));
        System.out.printf("Painel central - %s em (%.0f, %.0f)%n",
                notas.getTitulo(), container.getX(), container.getY());
        System.out.println("Barra direita - NavInfo:");
        escudo.getNavInfo().listarInfogramas()
                .forEach(item -> System.out.println("  " + item.getTitulo()));

        Infograma selecionado = escudo.getNavInfo().buscarInfograma(notas.getId()).orElseThrow();
        if (selecionado instanceof Documento documento) {
            System.out.println("Conteúdo selecionado: " + documento.getTexto());
        }

        TabelaRolavel encontros = new TabelaRolavel("Encontros na estrada");
        encontros.adicionarColuna("Criatura");
        encontros.adicionarColuna("Quantidade");
        Linha goblins = encontros.adicionarLinha(List.of("Goblins", "3"));
        Linha lobos = encontros.adicionarLinha(List.of("Lobos", "2"));
        encontros.definirChance(goblins.getId(), 1, 3);
        encontros.definirChance(lobos.getId(), 4, 6);
        escudo.getNavInfo().adicionarInfograma(encontros);
        escudo.getPainel().adicionarContainer(encontros.getId(), 350, 40);
        System.out.println(encontros.gerarTabelaTexto());
        System.out.println("Encontro sorteado: " + encontros.rolarResultado());

        BancoNomes nomes = new BancoNomes("Nomes de viajantes");
        nomes.adicionarNome("Elora");
        nomes.adicionarNome("Darin");
        nomes.adicionarSobrenome("Vale Verde");
        escudo.getNavInfo().adicionarInfograma(nomes);
        escudo.getPainel().adicionarContainer(nomes.getId(), 650, 40);
        System.out.println("Nome sorteado: " + nomes.gerarNome());
    }
}
