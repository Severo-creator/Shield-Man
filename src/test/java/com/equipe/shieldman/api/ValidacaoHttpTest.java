package com.equipe.shieldman.api;

import com.equipe.shieldman.infra.config.JacksonConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import java.sql.SQLException;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class ValidacaoHttpTest {
    @Test
    void beanValidationValidaCorposSemDependerDoBanco() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            assertFalse(validator.validate(new MestreResource.NomePedido(null)).isEmpty());
            assertFalse(validator.validate(new MestreResource.NomePedido(" ")).isEmpty());
            assertFalse(validator.validate(new MestreResource.NomePedido("a".repeat(121))).isEmpty());
            assertTrue(validator.validate(new MestreResource.NomePedido("Guilherme")).isEmpty());
            assertFalse(validator.validate(new EscudoResource.CriarEscudoPedido(null, "Campanha")).isEmpty());
            assertFalse(validator.validate(new EscudoResource.CriarEscudoPedido("invalido", "Campanha")).isEmpty());
            assertFalse(validator.validate(new EscudoResource.NomePedido("a".repeat(161))).isEmpty());
            assertTrue(validator.validate(new EscudoResource.NomePedido("a".repeat(160))).isEmpty());
            assertTrue(validator.validate(new EscudoResource.CriarEscudoPedido(UUID.randomUUID().toString(), "Campanha")).isEmpty());

            var violacoes = validator.validate(new MestreResource.NomePedido(""));
            try (var resposta = new ApiExceptionMapper().toResponse(new jakarta.validation.ConstraintViolationException(violacoes))) {
                assertEquals(400, resposta.getStatus());
                assertTrue(((ApiExceptionMapper.Problema) resposta.getEntity()).detail().contains("nome"));
            }
        }
    }

    @Test
    void jsonRejeitaConversoesSilenciosasCamposDesconhecidosEConteudoExtra() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        new JacksonConfig().customize(mapper);
        for (String corpo : new String[]{"{\"nome\":123}", "{\"nome\":true}", "{\"nome\":1.5}",
                "{\"nome\":[]}", "{\"nome\":{}}", "{\"nome\":\"Mestre\",\"extra\":1}",
                "{\"nome\":\"Mestre\"} {}"}) {
            var erro = assertThrows(com.fasterxml.jackson.core.JsonProcessingException.class,
                    () -> mapper.readValue(corpo, MestreResource.NomePedido.class));
            try (var resposta = new ApiExceptionMapper().toResponse(erro)) {
                assertEquals(400, resposta.getStatus());
                assertEquals("application/problem+json", resposta.getMediaType().toString());
            }
        }
        assertEquals("Mestre", mapper.readValue("{\"nome\":\"Mestre\"}", MestreResource.NomePedido.class).nome());
    }

    @Test
    void restricoesDoBancoTemMensagemSeguraSemRetornar500() {
        for (String state : new String[]{"23502", "23514", "22001", "23503", "23505", "23P01"}) {
            var erro = new RuntimeException("wrapper", new SQLException("SQL e valores privados", state));
            try (var resposta = new ApiExceptionMapper().toResponse(erro)) {
                int esperado = state.equals("23502") || state.equals("23514") || state.equals("22001") ? 400 : 409;
                assertEquals(esperado, resposta.getStatus());
                var problema = (ApiExceptionMapper.Problema) resposta.getEntity();
                assertEquals(esperado, problema.status());
                assertEquals("about:blank", problema.type());
                assertFalse(problema.title().isBlank());
                assertFalse(problema.detail().isBlank());
                assertFalse(problema.detail().contains("privados"));
            }
        }
    }
}
