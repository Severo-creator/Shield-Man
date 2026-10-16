package com.equipe.shieldman.api;

import com.equipe.shieldman.application.ConsultarEscudo;
import com.equipe.shieldman.application.GerenciarEscudos;
import com.equipe.shieldman.application.RecursoNaoEncontradoException;
import com.equipe.shieldman.domain.model.Escudo;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Pattern;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.UUID;
import java.util.List;
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

@Path("/escudos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Escudos", description = "Cadastro dos escudos e seu vínculo com o mestre.")
@APIResponses({
    @APIResponse(responseCode = "400", description = "Corpo, UUID ou parâmetros inválidos.",
        content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiExceptionMapper.Problema.class))),
    @APIResponse(responseCode = "415", description = "Formato de corpo não suportado; envie application/json.",
        content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiExceptionMapper.Problema.class))),
    @APIResponse(responseCode = "500", description = "Falha interna inesperada.",
        content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiExceptionMapper.Problema.class)))
})
public class EscudoResource {
    private final ConsultarEscudo consultarEscudo;
    private final GerenciarEscudos escudos;

    @Inject
    public EscudoResource(ConsultarEscudo consultarEscudo, GerenciarEscudos escudos) {
        this.consultarEscudo = consultarEscudo;
        this.escudos = escudos;
    }

    @POST
    @Operation(operationId = "criarEscudo", summary = "Cadastrar escudo", description = "O mestre informado deve existir.")
    @APIResponse(responseCode = "201", description = "Escudo criado.",
        headers = @Header(name = "Location", description = "Endereço para consultar o escudo criado.", schema = @Schema(type = SchemaType.STRING, format = "uri")),
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = EscudoResposta.class)))
    @APIResponse(responseCode = "404", description = "Mestre não encontrado.", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiExceptionMapper.Problema.class)))
    @APIResponse(responseCode = "409", description = "Conflito com um registro existente.", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiExceptionMapper.Problema.class)))
    public Response criar(@RequestBody(required = true, description = "Nome do escudo e UUID de seu mestre.")
            @NotNull(message = "Informe um corpo JSON.") @Valid CriarEscudoPedido pedido, @Context UriInfo uri) {
        EntradaHttp.corpo(pedido);
        UUID mestreId = pedido.mestreId() == null ? null : EntradaHttp.uuid(pedido.mestreId());
        Escudo escudo = escudos.criar(mestreId, pedido.nome());
        return Response.created(uri.getAbsolutePathBuilder().path(escudo.getId().toString()).build())
                .entity(resposta(escudo)).build();
    }

    @GET
    @Operation(operationId = "listarEscudos", summary = "Listar escudos",
        description = "Filtra por mestre e trecho literal do nome sem distinguir maiúsculas. Ordena por nome e UUID. Uma página sem resultados retorna [].")
    @APIResponse(responseCode = "200", description = "Página de escudos.",
        content = @Content(mediaType = "application/json", schema = @Schema(type = SchemaType.ARRAY, implementation = EscudoResposta.class)))
    public List<EscudoResposta> listar(@Parameter(description = "UUID do mestre; omita para consultar todos os mestres.", schema = @Schema(type = SchemaType.STRING, format = "uuid"))
            @QueryParam("mestreId") @Pattern(regexp = EntradaHttp.UUID_REGEX, message = "mestreId deve ser um UUID válido.") String mestreId,
            @Parameter(description = "Trecho do nome; filtro opcional.")
            @QueryParam("nome") @Size(max = 160, message = "Filtro de nome deve ter até 160 caracteres.") String nome,
            @Parameter(description = "Página, começando em zero.", schema = @Schema(type = SchemaType.INTEGER, format = "int32", minimum = "0", defaultValue = "0"))
            @QueryParam("pagina") @DefaultValue("0") @Min(value = 0, message = "Página deve ser não negativa.") int pagina,
            @Parameter(description = "Número máximo de resultados nesta página.", schema = @Schema(type = SchemaType.INTEGER, format = "int32", minimum = "1", maximum = "100", defaultValue = "20"))
            @QueryParam("tamanho") @DefaultValue("20") @Min(value = 1, message = "Tamanho deve ser pelo menos 1.")
            @Max(value = 100, message = "Tamanho deve ser no máximo 100.") int tamanho, @Context UriInfo uri) {
        EntradaHttp.parametros(uri);
        return escudos.listar(mestreId == null ? null : EntradaHttp.uuid(mestreId), nome, pagina, tamanho)
                .stream().map(EscudoResource::resposta).toList();
    }

    @GET
    @Path("/{id}")
    @Operation(operationId = "buscarEscudo", summary = "Consultar escudo pelo UUID")
    @APIResponse(responseCode = "200", description = "Escudo encontrado.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = EscudoResposta.class)))
    @APIResponse(responseCode = "404", description = "Escudo não encontrado.", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiExceptionMapper.Problema.class)))
    public Response buscarPorId(@Parameter(description = "UUID do escudo.", required = true, schema = @Schema(type = SchemaType.STRING, format = "uuid"))
            @PathParam("id") @Pattern(regexp = EntradaHttp.UUID_REGEX, message = "Identificador deve ser um UUID válido.") String id) {
        Escudo escudo = consultarEscudo.executar(EntradaHttp.uuid(id))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Escudo não encontrado."));
        return Response.ok(resposta(escudo)).build();
    }

    @PUT
    @Path("/{id}")
    @Operation(operationId = "atualizarEscudo", summary = "Renomear escudo", description = "Altera apenas o nome; o mestre do escudo não pode ser trocado.")
    @APIResponse(responseCode = "200", description = "Escudo atualizado.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = EscudoResposta.class)))
    @APIResponse(responseCode = "404", description = "Escudo não encontrado.", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiExceptionMapper.Problema.class)))
    public EscudoResposta atualizar(@Parameter(description = "UUID do escudo.", required = true, schema = @Schema(type = SchemaType.STRING, format = "uuid"))
            @PathParam("id") @Pattern(regexp = EntradaHttp.UUID_REGEX, message = "Identificador deve ser um UUID válido.") String id,
            @RequestBody(required = true, description = "Novo nome do escudo.") @NotNull(message = "Informe um corpo JSON.") @Valid NomePedido pedido) {
        return resposta(escudos.atualizar(EntradaHttp.uuid(id), EntradaHttp.corpo(pedido).nome()));
    }

    @DELETE
    @Path("/{id}")
    @Operation(operationId = "excluirEscudo", summary = "Excluir escudo")
    @APIResponse(responseCode = "204", description = "Escudo excluído; resposta sem corpo.")
    @APIResponse(responseCode = "404", description = "Escudo não encontrado.", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiExceptionMapper.Problema.class)))
    @APIResponse(responseCode = "409", description = "Conflito com registros relacionados.", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiExceptionMapper.Problema.class)))
    public Response excluir(@Parameter(description = "UUID do escudo.", required = true, schema = @Schema(type = SchemaType.STRING, format = "uuid"))
            @PathParam("id") @Pattern(regexp = EntradaHttp.UUID_REGEX, message = "Identificador deve ser um UUID válido.") String id) {
        escudos.excluir(EntradaHttp.uuid(id));
        return Response.noContent().build();
    }

    private static EscudoResposta resposta(Escudo escudo) {
        return new EscudoResposta(escudo.getId(), escudo.getMestreId(), escudo.getNome());
    }

    @Schema(name = "CriarEscudoPedido", requiredProperties = {"mestreId", "nome"})
    public record CriarEscudoPedido(
            @Schema(description = "UUID de um mestre já cadastrado.", format = "uuid", example = "123e4567-e89b-12d3-a456-426614174000")
            @NotBlank(message = "mestreId deve ser informado.")
            @Pattern(regexp = EntradaHttp.UUID_REGEX, message = "mestreId deve ser um UUID válido.") String mestreId,
            @Schema(description = "Nome do escudo.", example = "Minha campanha") @NotBlank(message = "Nome deve ser informado.")
            @Size(max = 160, message = "Nome deve ter até 160 caracteres.") String nome) { }

    @Schema(name = "NomeEscudoPedido", requiredProperties = {"nome"})
    public record NomePedido(
            @Schema(description = "Novo nome do escudo.", example = "Campanha atualizada")
            @NotBlank(message = "Nome deve ser informado.")
            @Size(max = 160, message = "Nome deve ter até 160 caracteres.") String nome) { }

    @Schema(name = "EscudoResposta", requiredProperties = {"id", "mestreId", "nome"})
    public record EscudoResposta(UUID id, UUID mestreId, String nome) {
    }
}
