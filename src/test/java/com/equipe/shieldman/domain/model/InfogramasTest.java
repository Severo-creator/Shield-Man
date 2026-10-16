package com.equipe.shieldman.domain.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class InfogramasTest {
    @Test
    void linhaEColunaCompartilhamACelulaERefletemEdicoes() {
        Tabela tabela = new Tabela("Equipamentos");
        Coluna item = tabela.adicionarColuna("Item");
        Coluna preco = tabela.adicionarColuna("Preço");
        Linha linha = tabela.adicionarLinha(List.of("Tocha", "1 moeda"));

        assertSame(linha.getCampos().getFirst(), item.getCampos().getFirst());
        assertEquals(linha.getId(), item.getCampos().getFirst().getLinhaId());
        assertEquals(item.getId(), linha.getCampos().getFirst().getColunaId());
        tabela.editarCampo(linha.getId(), preco.getId(), "2 moedas");
        assertEquals("2 moedas", preco.getCampos().getFirst().getCampo());
        assertEquals("Item | Preço\nTocha | 2 moedas", tabela.gerarTabelaTexto());

        Coluna peso = tabela.adicionarColuna("Peso");
        assertEquals(2, peso.getOrdem());
        assertSame(linha.getCampos().get(2), peso.getCampos().getFirst());
        assertEquals("", peso.getCampos().getFirst().getCampo());
        assertThrows(UnsupportedOperationException.class, () -> linha.getCampos().clear());
        assertThrows(UnsupportedOperationException.class, () -> tabela.getColunas().clear());
    }

    @Test
    void entradasInvalidasNaoDeixamATabelaParcialmenteModificada() {
        Tabela tabela = new Tabela("Tabela");
        Coluna coluna = tabela.adicionarColuna("Resultado");
        Linha linha = tabela.adicionarLinha(List.of("Nada acontece"));

        assertThrows(IllegalArgumentException.class, () -> tabela.adicionarLinha(List.of()));
        assertThrows(NullPointerException.class, () -> tabela.adicionarLinha(Arrays.asList((String) null)));
        assertThrows(IllegalArgumentException.class, () -> tabela.adicionarColuna(" "));
        assertThrows(IllegalArgumentException.class,
                () -> tabela.editarCampo(linha.getId(), UUID.randomUUID(), "Outro"));
        assertThrows(NullPointerException.class,
                () -> tabela.editarCampo(linha.getId(), coluna.getId(), null));
        assertEquals(1, tabela.getLinhas().size());
        assertEquals(1, tabela.getColunas().size());
        assertEquals("Nada acontece", coluna.getCampos().getFirst().getCampo());
    }

    @Test
    void resultadoDaRolagemUsaFaixasInclusivasETextoAtualizadoDaLinha() {
        TabelaRolavel tabela = new TabelaRolavel("Encontros");
        Coluna coluna = tabela.adicionarColuna("Criatura");
        Linha goblin = tabela.adicionarLinha(List.of("Goblin"));
        Linha lobo = tabela.adicionarLinha(List.of("Lobo"));
        tabela.definirChance(goblin.getId(), 1, 3);
        tabela.definirChance(lobo.getId(), 4, 6);

        assertEquals("Goblin", tabela.resolverResultado(1));
        assertEquals("Goblin", tabela.resolverResultado(3));
        assertEquals("Lobo", tabela.resolverResultado(4));
        assertEquals("Lobo", tabela.resolverResultado(6));
        assertThrows(IllegalArgumentException.class, () -> tabela.resolverResultado(7));
        tabela.editarCampo(lobo.getId(), coluna.getId(), "Lobo cinzento");

        // Seleciona o último número para verificar também o limite exclusivo do sorteio.
        Random ultimoResultado = new Random(0) {
            @Override
            public long nextLong(long origin, long bound) {
                assertEquals(1, origin);
                assertEquals(7, bound);
                return bound - 1;
            }
        };
        assertEquals("Lobo cinzento", tabela.rolarResultado(ultimoResultado));
    }

    @Test
    void faixasSobrepostasInvalidasOuDeOutraTabelaSaoRejeitadasSemAlteracao() {
        TabelaRolavel tabela = new TabelaRolavel("Encontros");
        tabela.adicionarColuna("Criatura");
        Linha primeira = tabela.adicionarLinha(List.of("Goblin"));
        Linha segunda = tabela.adicionarLinha(List.of("Lobo"));
        tabela.definirChance(primeira.getId(), 1, 3);
        tabela.definirChance(segunda.getId(), 4, 6);

        assertThrows(IllegalArgumentException.class, () -> tabela.definirChance(segunda.getId(), 3, 6));
        assertThrows(IllegalArgumentException.class, () -> tabela.definirChance(segunda.getId(), 0, 6));
        assertThrows(IllegalArgumentException.class, () -> tabela.definirChance(segunda.getId(), 6, 4));
        assertThrows(IllegalArgumentException.class, () -> tabela.definirChance(UUID.randomUUID(), 7, 8));
        assertEquals("Lobo", tabela.resolverResultado(4));
        tabela.definirChance(segunda.getId(), 4, 8);
        assertEquals("Lobo", tabela.resolverResultado(8));
        assertEquals(2, tabela.getChances().size());
    }

    @Test
    void sorteioExigeTodasAsLinhasConfiguradasSemLacunas() {
        TabelaRolavel tabela = new TabelaRolavel("Encontros");
        assertThrows(IllegalStateException.class, tabela::rolarResultado);
        tabela.adicionarColuna("Criatura");
        Linha primeira = tabela.adicionarLinha(List.of("Goblin"));
        Linha segunda = tabela.adicionarLinha(List.of("Lobo"));
        tabela.definirChance(primeira.getId(), 1, 3);
        assertThrows(IllegalStateException.class, tabela::rolarResultado);
        tabela.definirChance(segunda.getId(), 5, 6);
        assertThrows(IllegalStateException.class, tabela::rolarResultado);
        tabela.definirChance(segunda.getId(), 4, Integer.MAX_VALUE);
        assertDoesNotThrow(() -> tabela.rolarResultado(new Random(10)));
    }

    @Test
    void bancoGeraNomeComSobrenomeOpcionalERejeitaEntradasVazias() {
        BancoNomes banco = new BancoNomes("Nomes élficos");
        assertThrows(IllegalStateException.class, banco::gerarNome);
        assertThrows(IllegalArgumentException.class, () -> banco.adicionarNome(" "));
        assertThrows(IllegalArgumentException.class, () -> banco.adicionarSobrenome(null));
        banco.adicionarNome(" Elora ");
        assertEquals("Elora", banco.gerarNome(new Random(0)));
        banco.adicionarSobrenome("Folha Clara");
        assertEquals("Elora Folha Clara", banco.gerarNome(new Random(0)));
        assertThrows(UnsupportedOperationException.class, () -> banco.getNomes().clear());
    }

    @Test
    void containerBuscaCadaNovoTipoNoNavInfoDoSeuEscudo() {
        Escudo escudo = new Mestre("Mestre").criarEscudo("Campanha");
        for (Infograma infograma : List.of(new Tabela("Tabela"), new TabelaRolavel("Encontros"),
                new BancoNomes("Nomes"))) {
            assertThrows(IllegalArgumentException.class,
                    () -> escudo.getPainel().adicionarContainer(infograma.getId(), 0, 0));
            escudo.getNavInfo().adicionarInfograma(infograma);
            Container container = escudo.getPainel().adicionarContainer(infograma.getId(), 0, 0);
            assertSame(infograma, container.getInfograma());
        }
        assertEquals(3, escudo.getPainel().getContainers().size());
    }

    @Test
    void construtorDoContainerConsultaDiretamenteONavInfoRecebido() {
        NavInfo navInfo = new NavInfo();
        Documento documento = new Documento("Notas", "");
        assertThrows(IllegalArgumentException.class,
                () -> new Container(UUID.randomUUID(), navInfo, documento.getId(), 0, 0));
        navInfo.adicionarInfograma(documento);
        assertSame(documento,
                new Container(UUID.randomUUID(), navInfo, documento.getId(), 0, 0).getInfograma());
    }
}
