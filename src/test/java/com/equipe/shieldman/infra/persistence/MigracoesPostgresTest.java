package com.equipe.shieldman.infra.persistence;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** Usa um schema exclusivo por teste; não altera o schema public da aplicação. */
@EnabledIfEnvironmentVariable(named = "SHIELDMAN_TEST_MIGRATIONS", matches = "true")
class MigracoesPostgresTest {
    private String schema;
    private Connection connection;

    @BeforeEach
    void preparar() throws SQLException {
        schema = "shieldman_test_" + UUID.randomUUID().toString().replace("-", "");
        connection = DriverManager.getConnection(System.getenv("DB_URL"),
                System.getenv("DB_USER"), System.getenv("DB_PASSWORD"));
    }

    private Flyway flyway(String target) {
        return Flyway.configure().dataSource(System.getenv("DB_URL"),
                        System.getenv("DB_USER"), System.getenv("DB_PASSWORD"))
                .locations("classpath:db/migration").schemas(schema).defaultSchema(schema)
                .target(target).cleanDisabled(true).load();
    }

    private void executar(String sql) throws SQLException {
        connection.setSchema(schema);
        try (var statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    private long contar(String sql) throws SQLException {
        connection.setSchema(schema);
        try (var statement = connection.createStatement(); var result = statement.executeQuery(sql)) {
            result.next();
            return result.getLong(1);
        }
    }

    @AfterEach
    void limpar() throws SQLException {
        if (connection != null) {
            try (Connection aberta = connection; var statement = aberta.createStatement()) {
                if (schema.matches("shieldman_test_[a-f0-9]{32}")) {
                    statement.execute("DROP SCHEMA IF EXISTS " + schema + " CASCADE");
                }
            }
        }
    }

    @Test
    void bancoVazioRecebeTodoEsquemaERodarNovamenteNaoReaplicaMigracoes() throws SQLException {
        Flyway flyway = flyway("latest");
        assertEquals(2, flyway.migrate().migrationsExecuted);
        assertEquals(2, contar("SELECT count(*) FROM flyway_schema_history WHERE version IS NOT NULL AND success"));
        assertEquals(16, contar("SELECT count(*) FROM information_schema.tables WHERE table_schema = '"
                + schema + "' AND table_name <> 'flyway_schema_history'"));
        assertEquals(0, flyway.migrate().migrationsExecuted);
    }

    @Test
    void atualizarBancoV1PreservaCadastrosEPreparaPainelENavinfo() throws SQLException {
        assertEquals(1, flyway("1").migrate().migrationsExecuted);
        executar("""
                INSERT INTO mestre VALUES ('00000000-0000-0000-0000-000000000001', 'Mestre');
                INSERT INTO escudo VALUES ('00000000-0000-0000-0000-000000000002', 'Campanha',
                    '00000000-0000-0000-0000-000000000001');
                """);
        assertEquals(1, flyway("latest").migrate().migrationsExecuted);
        assertEquals(1, contar("SELECT count(*) FROM escudo WHERE nome = 'Campanha'"));
        assertEquals(1, contar("SELECT count(*) FROM navinfo"));
        assertEquals(1, contar("SELECT count(*) FROM painel"));
    }

    @Test
    void restricoesProtegemEscudoTipoPosicoesECelulas() throws SQLException {
        flyway("latest").migrate();
        executar("""
                INSERT INTO mestre VALUES ('00000000-0000-0000-0000-000000000001', 'Mestre');
                INSERT INTO escudo VALUES
                    ('00000000-0000-0000-0000-000000000002', 'Primeiro', '00000000-0000-0000-0000-000000000001'),
                    ('00000000-0000-0000-0000-000000000003', 'Segundo', '00000000-0000-0000-0000-000000000001');
                INSERT INTO navinfo SELECT id FROM escudo;
                INSERT INTO painel SELECT id, id FROM escudo;
                INSERT INTO infograma VALUES
                    ('00000000-0000-0000-0000-000000000010', '00000000-0000-0000-0000-000000000002', 'Notas', 'DOCUMENTO', 0),
                    ('00000000-0000-0000-0000-000000000011', '00000000-0000-0000-0000-000000000003', 'Tabela A', 'TABELA', 0),
                    ('00000000-0000-0000-0000-000000000012', '00000000-0000-0000-0000-000000000003', 'Tabela B', 'TABELA_ROLAVEL', 1);
                INSERT INTO documento(infograma_id, texto) VALUES ('00000000-0000-0000-0000-000000000010', 'Notas salvas');
                INSERT INTO tabela VALUES ('00000000-0000-0000-0000-000000000011', 'TABELA'),
                    ('00000000-0000-0000-0000-000000000012', 'TABELA_ROLAVEL');
                INSERT INTO tabela_rolavel(tabela_id) VALUES ('00000000-0000-0000-0000-000000000012');
                INSERT INTO linha VALUES ('00000000-0000-0000-0000-000000000020', '00000000-0000-0000-0000-000000000011', 0);
                INSERT INTO coluna VALUES ('00000000-0000-0000-0000-000000000021', '00000000-0000-0000-0000-000000000012', 'Coluna', 0);
                """);
        String container = """
                INSERT INTO container VALUES ('00000000-0000-0000-0000-000000000030',
                '00000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000002',
                '%s', 0, %s, 0, 240, 160)
                """;
        assertThrows(SQLException.class, () -> executar(container.formatted(
                "00000000-0000-0000-0000-000000000011", "0")));
        for (String x : new String[]{"-1", "'NaN'", "'Infinity'"}) {
            assertThrows(SQLException.class, () -> executar(container.formatted(
                    "00000000-0000-0000-0000-000000000010", x)));
        }
        assertThrows(SQLException.class, () -> executar("""
                INSERT INTO documento(infograma_id) VALUES ('00000000-0000-0000-0000-000000000011')
                """));
        assertThrows(SQLException.class, () -> executar("""
                INSERT INTO valor_campo VALUES ('00000000-0000-0000-0000-000000000020',
                    '00000000-0000-0000-0000-000000000021', '00000000-0000-0000-0000-000000000011', 'Inválido')
                """));
        executar(container.formatted("00000000-0000-0000-0000-000000000010", "0"));
        assertThrows(SQLException.class, () -> executar("""
                UPDATE container SET altura = 0
                """));
        executar("DELETE FROM container");
        assertEquals(1, contar("SELECT count(*) FROM documento WHERE texto = 'Notas salvas'"));
    }

    @Test
    void violacoesReaisDoPostgresSaoConvertidasEmProblemDetails() throws SQLException {
        flyway("latest").migrate();
        executar("INSERT INTO mestre VALUES ('00000000-0000-0000-0000-000000000001', 'Mestre')");
        String[] comandos = {
                "INSERT INTO mestre VALUES ('00000000-0000-0000-0000-000000000002', NULL)",
                "INSERT INTO mestre VALUES ('00000000-0000-0000-0000-000000000002', '')",
                "INSERT INTO mestre VALUES ('00000000-0000-0000-0000-000000000002', repeat('a', 121))",
                "INSERT INTO mestre VALUES ('00000000-0000-0000-0000-000000000001', 'Duplicado')",
                "INSERT INTO escudo VALUES ('00000000-0000-0000-0000-000000000003', 'Sem dono', '00000000-0000-0000-0000-000000000099')"
        };
        String[] estados = {"23502", "23514", "22001", "23505", "23503"};
        for (int i = 0; i < comandos.length; i++) {
            String comando = comandos[i];
            SQLException erro = assertThrows(SQLException.class, () -> executar(comando));
            assertEquals(estados[i], erro.getSQLState());
            try (var resposta = new com.equipe.shieldman.api.ApiExceptionMapper().toResponse(new RuntimeException(erro))) {
                assertEquals(i < 3 ? 400 : 409, resposta.getStatus());
                assertEquals("application/problem+json", resposta.getMediaType().toString());
                var problema = (com.equipe.shieldman.api.ApiExceptionMapper.Problema) resposta.getEntity();
                assertFalse(problema.detail().isBlank());
                assertFalse(problema.detail().contains("INSERT"));
            }
        }
    }
}
