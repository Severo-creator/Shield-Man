package com.equipe.shieldman.api;

import com.equipe.shieldman.application.GerenciarMestres;
import com.equipe.shieldman.domain.model.Mestre;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Pattern;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;

import java.util.List;
import java.util.UUID;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.headers.Header;

@Path("/mestres")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Mestres", description = "Cadastro dos mestres que possuem escudos.")
@APIResponses({
    @APIResponse(responseCode = "400", description = "Corpo, UUID ou parâmetros inválidos.",
        content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiExceptionMapper.Problema.class))),
    @APIResponse(responseCode = "415", description = "Formato de corpo não suportado; envie application/json.",
        content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiExceptionMapper.Problema.class))),
    @APIResponse(responseCode = "500", description = "Falha interna inesperada.",
        content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiExceptionMapper.Problema.class)))
})
public class MestreResource {
    private final GerenciarMestres mestres;

    @Inject
    public MestreResource(GerenciarMestres mestres) { this.mestres = mestres; }

    @POST
    @Operation(operationId = "criarMestre", summary = "Cadastrar mestre")
    @APIResponse(responseCode = "201", description = "Mestre criado.",
        headers = @Header(name = "Location", description = "Endereço para consultar o mestre criado.", schema = @Schema(type = SchemaType.STRING, format = "uri")),
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = MestreResposta.class)))
    @APIResponse(responseCode = "409", description = "Conflito com um registro existente.",
        content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiExceptionMapper.Problema.class)))
    public Response criar(@RequestBody(required = true, description = "Nome do mestre.")
            @NotNull(message = "Informe um corpo JSON.") @Valid NomePedido pedido, @Context UriInfo uri) {
        Mestre mestre = mestres.criar(EntradaHttp.corpo(pedido).nome());
        return Response.created(uri.getAbsolutePathBuilder().path(mestre.getId().toString()).build())
                .entity(resposta(mestre)).build();
    }

    @GET
    @Operation(operationId = "listarMestres", summary = "Listar mestres",
        description = "Filtra por trecho literal do nome sem distinguir maiúsculas. Ordena por nome e UUID. Uma página sem resultados retorna [].")
    @APIResponse(responseCode = "200", description = "Página de mestres.",
        content = @Content(mediaType = "application/json", schema = @Schema(type = SchemaType.ARRAY, implementation = MestreResposta.class)))
    public List<MestreResposta> listar(@Parameter(description = "Trecho do nome; filtro opcional.")
            @QueryParam("nome") @Size(max = 160, message = "Filtro de nome deve ter até 160 caracteres.") String nome,
            @Parameter(description = "Página, começando em zero.", schema = @Schema(type = SchemaType.INTEGER, format = "int32", minimum = "0", defaultValue = "0"))
            @QueryParam("pagina") @DefaultValue("0") @Min(value = 0, message = "Página deve ser não negativa.") int pagina,
            @Parameter(description = "Número máximo de resultados nesta página.", schema = @Schema(type = SchemaType.INTEGER, format = "int32", minimum = "1", maximum = "100", defaultValue = "20"))
            @QueryParam("tamanho") @DefaultValue("20") @Min(value = 1, message = "Tamanho deve ser pelo menos 1.")
            @Max(value = 100, message = "Tamanho deve ser no máximo 100.") int tamanho, @Context UriInfo uri) {
        EntradaHttp.parametros(uri);
        return mestres.listar(nome, pagina, tamanho).stream().map(MestreResource::resposta).toList();
    }

    @GET
    @Path("/{id}")
    @Operation(operationId = "buscarMestre", summary = "Consultar mestre pelo UUID")
    @APIResponse(responseCode = "200", description = "Mestre encontrado.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MestreResposta.class)))
    @APIResponse(responseCode = "404", description = "Mestre não encontrado.", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiExceptionMapper.Problema.class)))
    public MestreResposta buscar(@Parameter(description = "UUID do mestre.", required = true, schema = @Schema(type = SchemaType.STRING, format = "uuid"))
            @PathParam("id") @Pattern(regexp = EntradaHttp.UUID_REGEX, message = "Identificador deve ser um UUID válido.") String id) {
        return resposta(mestres.buscar(EntradaHttp.uuid(id)));
    }

    @PUT
    @Path("/{id}")
    @Operation(operationId = "atualizarMestre", summary = "Renomear mestre")
    @APIResponse(responseCode = "200", description = "Mestre atualizado.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MestreResposta.class)))
    @APIResponse(responseCode = "404", description = "Mestre não encontrado.", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiExceptionMapper.Problema.class)))
    public MestreResposta atualizar(@Parameter(description = "UUID do mestre.", required = true, schema = @Schema(type = SchemaType.STRING, format = "uuid"))
            @PathParam("id") @Pattern(regexp = EntradaHttp.UUID_REGEX, message = "Identificador deve ser um UUID válido.") String id,
            @RequestBody(required = true, description = "Novo nome do mestre.") @NotNull(message = "Informe um corpo JSON.") @Valid NomePedido pedido) {
        return resposta(mestres.atualizar(EntradaHttp.uuid(id), EntradaHttp.corpo(pedido).nome()));
    }

    @DELETE
    @Path("/{id}")
    @Operation(operationId = "excluirMestre", summary = "Excluir mestre", description = "Remova os escudos do mestre antes de excluí-lo.")
    @APIResponse(responseCode = "204", description = "Mestre excluído; resposta sem corpo.")
    @APIResponse(responseCode = "404", description = "Mestre não encontrado.", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiExceptionMapper.Problema.class)))
    @APIResponse(responseCode = "409", description = "Mestre ainda possui escudos.", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiExceptionMapper.Problema.class)))
    public Response excluir(@Parameter(description = "UUID do mestre.", required = true, schema = @Schema(type = SchemaType.STRING, format = "uuid"))
            @PathParam("id") @Pattern(regexp = EntradaHttp.UUID_REGEX, message = "Identificador deve ser um UUID válido.") String id) {
        mestres.excluir(EntradaHttp.uuid(id));
        return Response.noContent().build();
    }

    private static MestreResposta resposta(Mestre mestre) {
        return new MestreResposta(mestre.getId(), mestre.getNome());
    }

    @Schema(name = "NomeMestrePedido", description = "Dados para cadastrar ou renomear um mestre.", requiredProperties = {"nome"})
    public record NomePedido(
            @Schema(description = "Nome do mestre.", example = "Guilherme")
            @NotBlank(message = "Nome deve ser informado.")
            @Size(max = 120, message = "Nome deve ter até 120 caracteres.") String nome) { }
    @Schema(name = "MestreResposta", requiredProperties = {"id", "nome"})
    public record MestreResposta(UUID id, String nome) { }
}
