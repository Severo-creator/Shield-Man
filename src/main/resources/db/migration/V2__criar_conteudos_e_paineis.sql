-- A coleção do NavInfo é identificada pelo escudo ao qual pertence.
CREATE TABLE navinfo (
    escudo_id UUID PRIMARY KEY REFERENCES escudo(id) ON DELETE CASCADE
);

CREATE TABLE painel (
    id UUID PRIMARY KEY,
    escudo_id UUID NOT NULL UNIQUE REFERENCES navinfo(escudo_id) ON DELETE CASCADE,
    CONSTRAINT uq_painel_escudo UNIQUE (id, escudo_id)
);

CREATE TABLE infograma (
    id UUID PRIMARY KEY,
    escudo_id UUID NOT NULL REFERENCES navinfo(escudo_id) ON DELETE CASCADE,
    titulo TEXT NOT NULL CHECK (length(trim(titulo)) > 0),
    tipo VARCHAR(20) NOT NULL CHECK (tipo IN ('DOCUMENTO', 'TABELA', 'TABELA_ROLAVEL', 'BANCO_NOMES')),
    ordem INTEGER NOT NULL CHECK (ordem >= 0),
    CONSTRAINT uq_infograma_ordem UNIQUE (escudo_id, ordem),
    CONSTRAINT uq_infograma_escudo UNIQUE (id, escudo_id),
    CONSTRAINT uq_infograma_tipo UNIQUE (id, tipo)
);

CREATE TABLE container (
    id UUID PRIMARY KEY,
    painel_id UUID NOT NULL,
    escudo_id UUID NOT NULL,
    infograma_id UUID NOT NULL,
    ordem INTEGER NOT NULL CHECK (ordem >= 0),
    x DOUBLE PRECISION NOT NULL CHECK (x >= 0 AND x < 'Infinity'::DOUBLE PRECISION),
    y DOUBLE PRECISION NOT NULL CHECK (y >= 0 AND y < 'Infinity'::DOUBLE PRECISION),
    largura DOUBLE PRECISION NOT NULL CHECK (largura > 0 AND largura < 'Infinity'::DOUBLE PRECISION),
    altura DOUBLE PRECISION NOT NULL CHECK (altura > 0 AND altura < 'Infinity'::DOUBLE PRECISION),
    CONSTRAINT fk_container_painel FOREIGN KEY (painel_id, escudo_id)
        REFERENCES painel(id, escudo_id) ON DELETE CASCADE,
    CONSTRAINT fk_container_infograma FOREIGN KEY (infograma_id, escudo_id)
        REFERENCES infograma(id, escudo_id) ON DELETE CASCADE,
    CONSTRAINT uq_container_infograma UNIQUE (painel_id, infograma_id),
    CONSTRAINT uq_container_ordem UNIQUE (painel_id, ordem)
);

CREATE INDEX idx_container_infograma ON container(infograma_id, escudo_id);

-- Os tipos fixos e FKs compostas impedem associar um subtipo ao tipo errado.
CREATE TABLE documento (
    infograma_id UUID PRIMARY KEY,
    tipo VARCHAR(20) NOT NULL DEFAULT 'DOCUMENTO' CHECK (tipo = 'DOCUMENTO'),
    texto TEXT NOT NULL DEFAULT '',
    CONSTRAINT fk_documento_infograma FOREIGN KEY (infograma_id, tipo)
        REFERENCES infograma(id, tipo) ON DELETE CASCADE
);

CREATE TABLE tabela (
    infograma_id UUID PRIMARY KEY,
    tipo VARCHAR(20) NOT NULL CHECK (tipo IN ('TABELA', 'TABELA_ROLAVEL')),
    CONSTRAINT fk_tabela_infograma FOREIGN KEY (infograma_id, tipo)
        REFERENCES infograma(id, tipo) ON DELETE CASCADE,
    CONSTRAINT uq_tabela_tipo UNIQUE (infograma_id, tipo)
);

CREATE TABLE tabela_rolavel (
    tabela_id UUID PRIMARY KEY,
    tipo VARCHAR(20) NOT NULL DEFAULT 'TABELA_ROLAVEL' CHECK (tipo = 'TABELA_ROLAVEL'),
    CONSTRAINT fk_rolavel_tabela FOREIGN KEY (tabela_id, tipo)
        REFERENCES tabela(infograma_id, tipo) ON DELETE CASCADE
);

CREATE TABLE linha (
    id UUID PRIMARY KEY,
    tabela_id UUID NOT NULL REFERENCES tabela(infograma_id) ON DELETE CASCADE,
    ordem INTEGER NOT NULL CHECK (ordem >= 0),
    CONSTRAINT uq_linha_ordem UNIQUE (tabela_id, ordem),
    CONSTRAINT uq_linha_tabela UNIQUE (id, tabela_id)
);

CREATE TABLE coluna (
    id UUID PRIMARY KEY,
    tabela_id UUID NOT NULL REFERENCES tabela(infograma_id) ON DELETE CASCADE,
    titulo TEXT NOT NULL CHECK (length(trim(titulo)) > 0),
    ordem INTEGER NOT NULL CHECK (ordem >= 0),
    CONSTRAINT uq_coluna_ordem UNIQUE (tabela_id, ordem),
    CONSTRAINT uq_coluna_tabela UNIQUE (id, tabela_id)
);

CREATE TABLE valor_campo (
    linha_id UUID NOT NULL,
    coluna_id UUID NOT NULL,
    tabela_id UUID NOT NULL,
    campo TEXT NOT NULL DEFAULT '',
    PRIMARY KEY (linha_id, coluna_id),
    CONSTRAINT fk_campo_linha FOREIGN KEY (linha_id, tabela_id)
        REFERENCES linha(id, tabela_id) ON DELETE CASCADE,
    CONSTRAINT fk_campo_coluna FOREIGN KEY (coluna_id, tabela_id)
        REFERENCES coluna(id, tabela_id) ON DELETE CASCADE
);

CREATE INDEX idx_valor_campo_coluna ON valor_campo(coluna_id, tabela_id);
CREATE INDEX idx_valor_campo_tabela ON valor_campo(tabela_id);

CREATE TABLE valor_chance (
    linha_id UUID PRIMARY KEY,
    tabela_id UUID NOT NULL REFERENCES tabela_rolavel(tabela_id) ON DELETE CASCADE,
    valor_minimo INTEGER NOT NULL CHECK (valor_minimo >= 1),
    valor_maximo INTEGER NOT NULL CHECK (valor_maximo >= valor_minimo),
    CONSTRAINT fk_chance_linha FOREIGN KEY (linha_id, tabela_id)
        REFERENCES linha(id, tabela_id) ON DELETE CASCADE
);

CREATE INDEX idx_valor_chance_tabela ON valor_chance(tabela_id);

CREATE TABLE banco_nomes (
    infograma_id UUID PRIMARY KEY,
    tipo VARCHAR(20) NOT NULL DEFAULT 'BANCO_NOMES' CHECK (tipo = 'BANCO_NOMES'),
    CONSTRAINT fk_banco_nomes_infograma FOREIGN KEY (infograma_id, tipo)
        REFERENCES infograma(id, tipo) ON DELETE CASCADE
);

CREATE TABLE nome_banco (
    banco_nomes_id UUID NOT NULL REFERENCES banco_nomes(infograma_id) ON DELETE CASCADE,
    ordem INTEGER NOT NULL CHECK (ordem >= 0),
    nome TEXT NOT NULL CHECK (length(trim(nome)) > 0),
    PRIMARY KEY (banco_nomes_id, ordem)
);

CREATE TABLE sobrenome_banco (
    banco_nomes_id UUID NOT NULL REFERENCES banco_nomes(infograma_id) ON DELETE CASCADE,
    ordem INTEGER NOT NULL CHECK (ordem >= 0),
    sobrenome TEXT NOT NULL CHECK (length(trim(sobrenome)) > 0),
    PRIMARY KEY (banco_nomes_id, ordem)
);

-- Preserva os cadastros da V1 e prepara um NavInfo e um painel vazio para cada um.
-- O UUID do escudo também identifica seu painel inicial, em outra tabela.
INSERT INTO navinfo (escudo_id) SELECT id FROM escudo;
INSERT INTO painel (id, escudo_id) SELECT id, id FROM escudo;

-- Regras de conjunto continuam no domínio: preencher todas as células e configurar
-- faixas de rolagem sem lacunas nem sobreposição antes de realizar um sorteio.
