package com.equipe.shieldman.api;

import com.equipe.shieldman.application.ConflitoException;
import com.equipe.shieldman.application.RecursoNaoEncontradoException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Priorities;
import jakarta.validation.ConstraintViolationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import io.quarkus.hibernate.validator.runtime.jaxrs.ResteasyReactiveViolationException;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.sql.SQLException;
import java.util.stream.Collectors;
import org.jboss.logging.Logger;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Provider
public class ApiExceptionMapper implements ExceptionMapper<Exception> {
    private static final Logger LOG = Logger.getLogger(ApiExceptionMapper.class);
    @Context
    UriInfo uri;

    // Os tipos específicos prevalecem sobre os mapeadores padrão do Quarkus/Jackson.
    // Todos delegam ao mesmo tratamento abaixo para manter um único formato de erro.
    @ServerExceptionMapper(value = {ConstraintViolationException.class, ResteasyReactiveViolationException.class,
            JsonProcessingException.class, JsonParseException.class, MismatchedInputException.class,
            BadRequestException.class, NotFoundException.class}, priority = Priorities.USER - 100)
    public Response entradaInvalida(Exception erro) {
        return toResponse(erro);
    }

    @Override
    public Response toResponse(Exception erro) {
        int status = 500;
        String detalhe = "Não foi possível concluir a operação.";
        SQLException sql = causaSql(erro);
        if (erro instanceof ConstraintViolationException violacao) {
            status = 400;
            detalhe = violacao.getConstraintViolations().stream().map(item -> {
                String campo = "entrada";
                for (var node : item.getPropertyPath()) {
                    if (node.getName() != null) { campo = node.getName(); }
                }
                return campo + ": " + item.getMessage();
            }).distinct().sorted().collect(Collectors.joining("; "));
        } else if (temCausa(erro, JsonProcessingException.class)) {
            status = 400;
            detalhe = "JSON inválido: confira a sintaxe, os campos permitidos e seus tipos. Nome e mestreId devem ser textos.";
        } else if (sql != null && ("23502".equals(sql.getSQLState()) || "23514".equals(sql.getSQLState())
                || "22001".equals(sql.getSQLState()))) {
            status = 400;
            detalhe = switch (sql.getSQLState()) {
                case "23502" -> "Um campo obrigatório não foi informado.";
                case "22001" -> "Um dos textos excede o tamanho permitido.";
                default -> "Um dos valores não atende às regras do cadastro.";
            };
        } else if (sql != null && ("23503".equals(sql.getSQLState()) || "23505".equals(sql.getSQLState())
                || "23P01".equals(sql.getSQLState()))) {
            status = 409;
            detalhe = "A operação conflita com os registros existentes.";
        } else if (erro instanceof RecursoNaoEncontradoException) {
            status = 404;
            detalhe = erro.getMessage();
        } else if (erro instanceof ConflitoException) {
            status = 409;
            detalhe = erro.getMessage();
        } else if (erro instanceof IllegalArgumentException) {
            status = 400;
            detalhe = erro.getMessage();
        } else if (erro instanceof WebApplicationException web) {
            status = web.getResponse().getStatus();
            if (temCausa(erro, NumberFormatException.class)) {
                status = 400;
                detalhe = "Página e tamanho devem ser números inteiros válidos.";
            } else {
                detalhe = status == 400 ? "Requisição ou JSON inválido." : "A requisição não pôde ser atendida.";
            }
        }
        if (status >= 500) { LOG.error("Falha ao processar requisição", erro); }
        String titulo = switch (status) {
            case 400 -> "Entrada inválida";
            case 404 -> "Recurso não encontrado";
            case 409 -> "Conflito";
            default -> "Falha na requisição";
        };
        return Response.status(status).type("application/problem+json")
                .entity(new Problema("about:blank", titulo, status, detalhe,
                        uri == null ? null : uri.getRequestUri().getRawPath())).build();
    }

    private static SQLException causaSql(Throwable erro) {
        for (int i = 0; erro != null && i < 32; i++, erro = erro.getCause()) {
            if (erro instanceof SQLException sql) { return sql; }
        }
        return null;
    }

    private static boolean temCausa(Throwable erro, Class<? extends Throwable> tipo) {
        for (int i = 0; erro != null && i < 32; i++, erro = erro.getCause()) {
            if (tipo.isInstance(erro)) { return true; }
        }
        return false;
    }

    @Schema(name = "Problema", description = "Erro no formato problem details.", requiredProperties = {"type", "title", "status", "detail"})
    public record Problema(
            @Schema(example = "about:blank") String type,
            @Schema(example = "Entrada inválida") String title,
            @Schema(minimum = "400", maximum = "599", example = "400") int status,
            @Schema(example = "nome: Nome deve ser informado.") String detail,
            @Schema(example = "/mestres") String instance) { }
}
