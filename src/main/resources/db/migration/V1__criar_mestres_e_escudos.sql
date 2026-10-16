CREATE TABLE mestre (
                        id UUID PRIMARY KEY,
                        nome VARCHAR(120) NOT NULL,
                        CONSTRAINT ck_mestre_nome
                            CHECK (length(trim(nome)) > 0)
);

CREATE TABLE escudo (
                        id UUID PRIMARY KEY,
                        nome VARCHAR(160) NOT NULL,
                        mestre_id UUID NOT NULL,

                        CONSTRAINT ck_escudo_nome
                            CHECK (length(trim(nome)) > 0),

                        CONSTRAINT fk_escudo_mestre
                            FOREIGN KEY (mestre_id)
                                REFERENCES mestre (id)
);

CREATE INDEX idx_escudo_mestre_id
    ON escudo (mestre_id);