package com.equipe.shieldman.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** Confere a especificação gerada e a UI realmente servidas pela aplicação. */
@EnabledIfEnvironmentVariable(named = "SHIELDMAN_TEST_API", matches = "true")
class OpenApiHttpTest {
    private final HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(10)).build();
    private final String base = System.getenv().getOrDefault("SHIELDMAN_API_URL", "http://localhost:8080");
    private final ObjectMapper json = new ObjectMapper();

    private HttpResponse<String> get(String path) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create(base + path)).timeout(Duration.ofSeconds(45))
                .GET().build(), HttpResponse.BodyHandlers.ofString());
    }

    private JsonNode documento() throws Exception {
        var resposta = get("/q/openapi?format=json");
        assertEquals(200, resposta.statusCode(), resposta.body());
        return json.readTree(resposta.body());
    }

    @Test
    void especificacaoCobreTodasAsOperacoesDoCodigoESuasRespostas() throws Exception {
        JsonNode spec = documento();
        assertTrue(spec.get("openapi").asText().startsWith("3."));
        assertEquals("ShieldMan API", spec.at("/info/title").asText());
        Set<String> ids = new HashSet<>();
        int operacoes = 0;
        for (Class<?> resource : List.of(MestreResource.class, EscudoResource.class)) {
            String raiz = resource.getAnnotation(Path.class).value();
            for (var method : resource.getDeclaredMethods()) {
                for (var annotation : method.getDeclaredAnnotations()) {
                    HttpMethod verbo = annotation.annotationType().getAnnotation(HttpMethod.class);
                    if (verbo == null) { continue; }
                    String http = verbo.value().toLowerCase(Locale.ROOT);
                    String path = raiz + (method.isAnnotationPresent(Path.class) ? method.getAnnotation(Path.class).value() : "");
                    JsonNode operacao = spec.path("paths").path(path).path(http);
                    assertFalse(operacao.isMissingNode(), http + " " + path);
                    assertFalse(operacao.path("summary").asText().isBlank());
                    assertFalse(operacao.path("operationId").asText().isBlank());
                    assertTrue(ids.add(operacao.get("operationId").asText()));
                    assertFalse(operacao.path("tags").isEmpty());
                    String sucesso = switch (http) { case "post" -> "201"; case "delete" -> "204"; default -> "200"; };
                    JsonNode respostas = operacao.path("responses");
                    assertTrue(respostas.has(sucesso));
                    assertTrue(respostas.has("400"));
                    assertTrue(respostas.has("500"));
                    if (path.contains("{id}")) { assertTrue(respostas.has("404")); }
                    var nomesStatus = respostas.fieldNames();
                    while (nomesStatus.hasNext()) {
                        String status = nomesStatus.next();
                        if (status.startsWith("4") || status.startsWith("5")) {
                            assertEquals("#/components/schemas/Problema", respostas.path(status).path("content")
                                    .path("application/problem+json").path("schema").path("$ref").asText());
                        }
                    }
                    if (http.equals("post")) { assertTrue(respostas.path("201").path("headers").has("Location")); }
                    if (http.equals("delete")) { assertFalse(respostas.path("204").has("content")); }
                    if (http.equals("post") || http.equals("put")) {
                        assertTrue(operacao.path("requestBody").path("required").asBoolean());
                        assertTrue(operacao.path("requestBody").path("content").has("application/json"));
                    }
                    operacoes++;
                }
            }
        }
        assertEquals(10, operacoes);
        assertTrue(spec.path("paths").path("/mestres/{id}").path("delete").path("responses").has("409"));
        assertTrue(spec.path("paths").path("/escudos").path("post").path("responses").has("404"));
    }

    private JsonNode parametro(JsonNode operacao, String nome) {
        for (JsonNode p : operacao.path("parameters")) {
            if (p.path("name").asText().equals(nome)) { return p; }
        }
        fail("Parâmetro ausente na especificação: " + nome);
        return null;
    }

    @Test
    void parametrosCorposEModelosTemTiposELimitesCorretos() throws Exception {
        JsonNode spec = documento();
        for (String raiz : List.of("/mestres", "/escudos")) {
            JsonNode lista = spec.path("paths").path(raiz).path("get");
            JsonNode tamanho = parametro(lista, "tamanho").path("schema");
            assertEquals("integer", tamanho.path("type").asText());
            assertEquals(1, tamanho.path("minimum").asInt());
            assertEquals(100, tamanho.path("maximum").asInt());
            assertEquals(20, tamanho.path("default").asInt());
            JsonNode pagina = parametro(lista, "pagina").path("schema");
            assertEquals("integer", pagina.path("type").asText());
            assertEquals(0, pagina.path("minimum").asInt());
            JsonNode id = parametro(spec.path("paths").path(raiz + "/{id}").path("get"), "id");
            assertEquals("uuid", id.path("schema").path("format").asText());
            assertTrue(id.path("required").asBoolean());
            assertEquals("array", lista.path("responses").path("200").path("content")
                    .path("application/json").path("schema").path("type").asText());
        }
        assertEquals("uuid", parametro(spec.path("paths").path("/escudos").path("get"), "mestreId")
                .path("schema").path("format").asText());
        JsonNode schemas = spec.path("components").path("schemas");
        assertEquals(120, schemas.path("NomeMestrePedido").path("properties").path("nome").path("maxLength").asInt());
        assertEquals(160, schemas.path("NomeEscudoPedido").path("properties").path("nome").path("maxLength").asInt());
        assertEquals("uuid", schemas.path("CriarEscudoPedido").path("properties").path("mestreId").path("format").asText());
        for (String campo : List.of("type", "title", "status", "detail")) {
            assertTrue(schemas.path("Problema").path("properties").has(campo));
        }
    }

    @Test
    void yamlSwaggerUiESeusScriptsEstaoAcessiveis() throws Exception {
        var yaml = get("/q/openapi");
        assertEquals(200, yaml.statusCode());
        assertTrue(yaml.body().contains("openapi:"));
        var ui = get("/q/swagger-ui/");
        assertEquals(200, ui.statusCode(), ui.body());
        assertTrue(ui.headers().firstValue("Content-Type").orElseThrow().contains("text/html"));
        assertTrue(ui.body().contains("swagger-ui"));
        assertEquals(200, get("/q/swagger-ui/swagger-ui-bundle.js").statusCode());
    }
}
