package com.equipe.shieldman.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ModeloShieldManTest {
    @Test
    void mestreCriaRenomeiaERemoveSomenteSeusEscudos() {
        Mestre mestre = new Mestre("Guilherme");
        Escudo escudo = mestre.criarEscudo("Campanha");
        Escudo outro = new Mestre("Outro mestre").criarEscudo("Outra campanha");

        assertEquals(mestre.getId(), escudo.getMestreId());
        escudo.renomear("Nova campanha");
        assertEquals("Nova campanha", mestre.buscarEscudo(escudo.getId()).orElseThrow().getNome());
        assertThrows(IllegalArgumentException.class, () -> mestre.removerEscudo(outro.getId()));
        assertEquals(1, mestre.getEscudos().size());
        mestre.removerEscudo(escudo.getId());
        assertTrue(mestre.getEscudos().isEmpty());
    }

    @Test
    void containerConsultaNavInfoEPreservaConteudoAoSerRemovido() {
        Escudo escudo = new Mestre("Mestre").criarEscudo("Campanha");
        Documento documento = new Documento("Notas", "Texto inicial");
        escudo.getNavInfo().adicionarInfograma(documento);
        assertTrue(escudo.getPainel().getContainers().isEmpty());
        Container container = escudo.getPainel().adicionarContainer(documento.getId(), 10, 20);

        assertEquals(escudo.getPainel().getId(), container.getPainelId());
        assertSame(documento, container.getInfograma());
        assertSame(documento, escudo.getNavInfo().buscarInfograma(documento.getId()).orElseThrow());
        documento.renomear("Notas atualizadas");
        documento.editarTexto("Texto atualizado");
        Documento peloNavInfo = (Documento) escudo.getNavInfo().listarInfogramas().getFirst();
        assertEquals("Notas atualizadas", peloNavInfo.getTitulo());
        assertEquals("Texto atualizado", peloNavInfo.getTexto());

        escudo.getPainel().removerContainer(container.getId());
        assertTrue(escudo.getPainel().getContainers().isEmpty());
        assertSame(documento, escudo.getNavInfo().buscarInfograma(documento.getId()).orElseThrow());
        Container recriado = escudo.getPainel().adicionarContainer(documento.getId(), 30, 40);
        assertSame(documento, recriado.getInfograma());
    }

    @Test
    void escudosPossuemPaineisEListasDeNavegacaoIndependentes() {
        Mestre mestre = new Mestre("Mestre");
        Escudo primeiro = mestre.criarEscudo("Primeiro");
        Escudo segundo = mestre.criarEscudo("Segundo");
        Documento documento = new Documento("Notas", "");
        primeiro.getNavInfo().adicionarInfograma(documento);
        Container container = primeiro.getPainel().adicionarContainer(documento.getId(), 0, 0);

        assertNotEquals(primeiro.getPainel().getId(), segundo.getPainel().getId());
        assertTrue(segundo.getNavInfo().listarInfogramas().isEmpty());
        assertThrows(IllegalArgumentException.class,
                () -> segundo.getPainel().adicionarContainer(documento.getId(), 0, 0));
        assertTrue(segundo.getPainel().getContainers().isEmpty());
        assertThrows(IllegalArgumentException.class,
                () -> segundo.getPainel().removerContainer(container.getId()));
        assertEquals(1, primeiro.getPainel().getContainers().size());
    }

    @Test
    void movimentoERedimensionamentoValidamTudoAntesDeAlterarOContainer() {
        Escudo escudo = new Mestre("Mestre").criarEscudo("Campanha");
        Documento documento = new Documento("Notas", "");
        escudo.getNavInfo().adicionarInfograma(documento);
        Container container = escudo.getPainel().adicionarContainer(documento.getId(), 10, 20);
        container.moverPara(30, 40);
        container.redimensionar(300, 200);

        assertThrows(IllegalArgumentException.class, () -> container.moverPara(50, -1));
        assertThrows(IllegalArgumentException.class, () -> container.moverPara(Double.NaN, 0));
        assertThrows(IllegalArgumentException.class, () -> container.redimensionar(400, 0));
        assertThrows(IllegalArgumentException.class,
                () -> container.redimensionar(Double.POSITIVE_INFINITY, 100));
        assertEquals(30, container.getX());
        assertEquals(40, container.getY());
        assertEquals(300, container.getLargura());
        assertEquals(200, container.getAltura());
    }

    @Test
    void listasNaoPermitemContornarAsOperacoesDoModelo() {
        Mestre mestre = new Mestre("Mestre");
        Escudo escudo = mestre.criarEscudo("Campanha");
        Documento documento = new Documento("Notas", "");
        escudo.getNavInfo().adicionarInfograma(documento);
        escudo.getPainel().adicionarContainer(documento.getId(), 0, 0);

        assertThrows(UnsupportedOperationException.class, () -> mestre.getEscudos().clear());
        assertThrows(UnsupportedOperationException.class, () -> escudo.getPainel().getContainers().clear());
        assertThrows(UnsupportedOperationException.class, () -> escudo.getNavInfo().listarInfogramas().clear());
        assertThrows(IllegalArgumentException.class,
                () -> escudo.getPainel().adicionarContainer(documento.getId(), 20, 20));
        assertThrows(IllegalArgumentException.class,
                () -> escudo.getNavInfo().adicionarInfograma(documento));
        assertEquals(1, escudo.getNavInfo().listarInfogramas().size());
        assertEquals(1, escudo.getPainel().getContainers().size());
    }

    @Test
    void entradasInvalidasNaoModificamOsDadosExistentes() {
        Mestre mestre = new Mestre("Mestre");
        Escudo escudo = mestre.criarEscudo("Campanha");
        Documento documento = new Documento("Notas", "Texto");
        escudo.getNavInfo().adicionarInfograma(documento);

        assertThrows(IllegalArgumentException.class, () -> mestre.criarEscudo(" "));
        assertThrows(IllegalArgumentException.class, () -> escudo.renomear(null));
        assertThrows(IllegalArgumentException.class, () -> documento.renomear(""));
        assertThrows(NullPointerException.class, () -> documento.editarTexto(null));
        assertThrows(IllegalArgumentException.class,
                () -> escudo.getPainel().adicionarContainer(documento.getId(), -1, 0));
        assertThrows(NullPointerException.class, () -> escudo.getNavInfo().adicionarInfograma(null));
        assertThrows(NullPointerException.class,
                () -> escudo.getPainel().adicionarContainer(null, 0, 0));
        assertThrows(IllegalArgumentException.class,
                () -> escudo.getPainel().adicionarContainer(java.util.UUID.randomUUID(), 0, 0));
        assertEquals(1, mestre.getEscudos().size());
        assertEquals("Campanha", escudo.getNome());
        assertEquals("Notas", documento.getTitulo());
        assertEquals("Texto", documento.getTexto());
        assertTrue(escudo.getPainel().getContainers().isEmpty());
    }
}
