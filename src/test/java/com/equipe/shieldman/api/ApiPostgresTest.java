package com.equipe.shieldman.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** Testa HTTP e os repositórios reais da aplicação já iniciada. */
@EnabledIfEnvironmentVariable(named = "SHIELDMAN_TEST_API", matches = "true")
class ApiPostgresTest {
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private final ObjectMapper json = new ObjectMapper();
    private final String base = System.getenv().getOrDefault("SHIELDMAN_API_URL", "http://localhost:8080");
    private final String prefixo = "teste-t4-" + UUID.randomUUID();
    private final List<String> mestres = new ArrayList<>();
    private final List<String> escudos = new ArrayList<>();

    private HttpResponse<String> enviar(String metodo, String caminho, Object corpo) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create(base + caminho)).timeout(Duration.ofSeconds(30));
        builder.header("Accept", "application/json");
        var conteudo = corpo == null ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(corpo instanceof String texto ? texto : json.writeValueAsString(corpo));
        if (corpo != null) { builder.header("Content-Type", "application/json"); }
        return client.send(builder.method(metodo, conteudo).build(), HttpResponse.BodyHandlers.ofString());
    }

    private JsonNode dados(HttpResponse<String> resposta, int status) throws Exception {
        assertEquals(status, resposta.statusCode(), resposta.body());
        return json.readTree(resposta.body());
    }

    private String criarMestre(String nome) throws Exception {
        var resposta = enviar("POST", "/mestres", Map.of("nome", nome));
        String id = dados(resposta, 201).get("id").asText();
        mestres.add(id);
        assertTrue(resposta.headers().firstValue("Location").orElseThrow().endsWith("/mestres/" + id));
        return id;
    }

    private String criarEscudo(String mestreId, String nome) throws Exception {
        var resposta = enviar("POST", "/escudos", Map.of("mestreId", mestreId, "nome", nome));
        String id = dados(resposta, 201).get("id").asText();
        escudos.add(id);
        assertTrue(resposta.headers().firstValue("Location").orElseThrow().endsWith("/escudos/" + id));
        return id;
    }

    @AfterEach
    void limparSomenteCadastrosDesteTeste() throws Exception {
        for (String id : escudos) {
            int status = enviar("DELETE", "/escudos/" + id, null).statusCode();
            assertTrue(status == 204 || status == 404);
        }
        for (String id : mestres) {
            int status = enviar("DELETE", "/mestres/" + id, null).statusCode();
            assertTrue(status == 204 || status == 404);
        }
    }

    @Test
    void crudGravaConsultaAtualizaEExcluiNoPostgres() throws Exception {
        String mestreId = criarMestre(prefixo);
        String escudoId = criarEscudo(mestreId, prefixo + "-campanha");
        assertEquals(mestreId, dados(enviar("GET", "/escudos/" + escudoId, null), 200).get("mestreId").asText());
        assertEquals(prefixo, dados(enviar("GET", "/mestres/" + mestreId, null), 200).get("nome").asText());
        assertEquals(409, dados(enviar("DELETE", "/mestres/" + mestreId, null), 409).get("status").asInt());
        assertEquals("Atualizado", dados(enviar("PUT", "/mestres/" + mestreId, Map.of("nome", "Atualizado")), 200)
                .get("nome").asText());
        assertEquals("Nova campanha", dados(enviar("PUT", "/escudos/" + escudoId, Map.of("nome", "Nova campanha")), 200)
                .get("nome").asText());
        assertEquals("Nova campanha", dados(enviar("GET", "/escudos/" + escudoId, null), 200).get("nome").asText());
        assertEquals(204, enviar("DELETE", "/escudos/" + escudoId, null).statusCode());
        assertEquals(404, enviar("GET", "/escudos/" + escudoId, null).statusCode());
        assertEquals(204, enviar("DELETE", "/mestres/" + mestreId, null).statusCode());
        assertEquals(404, enviar("GET", "/mestres/" + mestreId, null).statusCode());
    }

    @Test
    void filtrosEPaginacaoSaoExecutadosPeloRepositorioReal() throws Exception {
        String mestreId = criarMestre(prefixo + "-dono");
        String outro = criarMestre(prefixo + "-outro");
        String primeiro = criarEscudo(mestreId, prefixo + "-A");
        String segundo = criarEscudo(mestreId, prefixo + "-B");
        criarEscudo(outro, prefixo + "-C");
        String filtro = "/escudos?mestreId=" + mestreId + "&nome=" + prefixo.toUpperCase() + "&tamanho=1&pagina=";
        assertEquals(primeiro, dados(enviar("GET", filtro + "0", null), 200).get(0).get("id").asText());
        assertEquals(segundo, dados(enviar("GET", filtro + "1", null), 200).get(0).get("id").asText());
        assertTrue(dados(enviar("GET", filtro + "2", null), 200).isEmpty());
        assertEquals(2, dados(enviar("GET", "/mestres?nome=" + prefixo, null), 200).size());
        criarEscudo(mestreId, prefixo + "-100%_");
        assertEquals(1, dados(enviar("GET", "/escudos?mestreId=" + mestreId + "&nome=100%25_", null), 200).size());
    }

    @Test
    void entradasInvalidasERecursosAusentesRetornamErrosSemGravar() throws Exception {
        String mestreId = criarMestre(prefixo);
        assertEquals(400, enviar("POST", "/mestres", Map.of("nome", " ")).statusCode());
        assertEquals(400, enviar("POST", "/mestres", Map.of("nome", "a".repeat(121))).statusCode());
        assertEquals(400, enviar("POST", "/mestres", "{").statusCode());
        assertEquals(400, enviar("POST", "/escudos", Map.of("nome", "Sem dono")).statusCode());
        assertEquals(400, enviar("POST", "/escudos", Map.of("nome", "a".repeat(161), "mestreId", mestreId)).statusCode());
        assertEquals(404, enviar("POST", "/escudos", Map.of("nome", "Inexistente", "mestreId", UUID.randomUUID().toString())).statusCode());
        var invalido = enviar("GET", "/escudos/UUID_DO_ESCUDO", null);
        assertEquals(400, dados(invalido, 400).get("status").asInt());
        assertTrue(invalido.headers().firstValue("Content-Type").orElseThrow().startsWith("application/problem+json"));
        assertEquals(400, enviar("GET", "/mestres?tamanho=101", null).statusCode());
        assertEquals(400, enviar("GET", "/escudos?pagina=-1", null).statusCode());
        assertEquals(404, enviar("PUT", "/escudos/" + UUID.randomUUID(), Map.of("nome", "Ausente")).statusCode());
        assertEquals(404, enviar("DELETE", "/mestres/" + UUID.randomUUID(), null).statusCode());
        assertTrue(dados(enviar("GET", "/escudos?mestreId=" + mestreId, null), 200).isEmpty());
    }

    private void conferirProblema(HttpResponse<String> resposta, int status) throws Exception {
        JsonNode problema = dados(resposta, status);
        assertTrue(resposta.headers().firstValue("Content-Type").orElseThrow().startsWith("application/problem+json"));
        assertEquals(status, problema.get("status").asInt());
        for (String campo : List.of("type", "title", "detail")) {
            assertTrue(problema.hasNonNull(campo));
            assertFalse(problema.get(campo).asText().isBlank());
        }
        assertTrue(problema.get("instance").asText().startsWith("/"));
        assertFalse(resposta.body().contains("stackTrace"));
        assertFalse(resposta.body().contains("org.hibernate"));
    }

    @Test
    void todosOsFormatosInvalidosRecebemProblemDetails() throws Exception {
        String mestreId = criarMestre(prefixo);
        String escudoId = criarEscudo(mestreId, "Campanha");
        for (String corpo : List.of("", "null", "{}", "[]", "{", "{\"nome\":null}",
                "{\"nome\":\" \"}", "{\"nome\":123}", "{\"nome\":true}", "{\"nome\":[]}",
                "{\"nome\":{}}", "{\"nome\":\"Mestre\",\"extra\":1}", "{\"nome\":\"Mestre\"} {}")) {
            conferirProblema(enviar("POST", "/mestres", corpo), 400);
        }
        conferirProblema(enviar("POST", "/escudos", Map.of("nome", "Campanha")), 400);
        conferirProblema(enviar("POST", "/escudos", Map.of("nome", "Campanha", "mestreId", "invalido")), 400);
        conferirProblema(enviar("PUT", "/mestres/" + mestreId, Map.of("nome", "a".repeat(121))), 400);
        conferirProblema(enviar("PUT", "/escudos/" + escudoId, "{}"), 400);
        for (String caminho : List.of("/mestres?pagina=abc", "/mestres?tamanho=999999999999999",
                "/mestres?pagina=-1", "/mestres?pagina=", "/mestres?tamanho=", "/mestres?tamanho=0",
                "/mestres?tamanho=101", "/mestres?nome=" + "a".repeat(161),
                "/escudos?mestreId=invalido", "/escudos?mestreId=", "/escudos/UUID_DO_ESCUDO", "/mestres/invalido")) {
            conferirProblema(enviar("GET", caminho, null), 400);
        }
        conferirProblema(enviar("GET", "/mestres/" + UUID.randomUUID(), null), 404);
        conferirProblema(enviar("DELETE", "/mestres/" + mestreId, null), 409);
        assertEquals(1, dados(enviar("GET", "/mestres?nome=" + prefixo, null), 200).size());
    }
}
