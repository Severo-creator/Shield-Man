# Validação e problem details (T7)

A API de mestres e escudos usa HTTP 400 para entradas inválidas. A extensão `quarkus-hibernate-validator` aplica Bean Validation nos pedidos e parâmetros da camada HTTP. O domínio e os casos de uso continuam sem imports de Jakarta ou Quarkus; suas validações Java continuam protegendo usos fora de HTTP.

## Validações

| Entrada | Regra |
| --- | --- |
| Corpos de POST e PUT | Obrigatórios, JSON com objeto de campos conhecidos |
| Nome do mestre | `@NotBlank`, `@Size(max = 120)` |
| Nome do escudo | `@NotBlank`, `@Size(max = 160)` |
| mestreId ao criar escudo | `@NotBlank` e UUID completo |
| UUID no caminho ou no filtro | Formato completo com hífens; filtro pode estar ausente |
| nome na listagem | Até 160 caracteres; filtro vazio é permitido |
| pagina | Inteiro não negativo; padrão 0 |
| tamanho | Inteiro entre 1 e 100; padrão 20 |
| Parâmetros pagina, tamanho e mestreId fornecidos | Um único valor não vazio |

Os corpos usam `@Valid` e `@NotNull`; as regras dos campos são declaradas nos records de pedido. Parâmetros usam `@Pattern`, `@Min`, `@Max` e `@Size`. A checagem explícita da URI impede que o framework transforme `?mestreId=` ou paginação vazia em ausência ou valor padrão. A checagem de paginação nos casos de uso também impede estouro do deslocamento.

`JacksonConfig` rejeita propriedades desconhecidas, conteúdo extra depois do objeto e conversão automática de números ou booleanos para texto. Um pedido como `{"nome":123}` recebe 400, em vez de criar um mestre chamado `123`. PUT de mestre e PUT de escudo têm pedidos separados para respeitar seus respectivos limites de nome.

## Tratamento centralizado

`ApiExceptionMapper` concentra a criação das respostas. Os registros específicos de exceções do Quarkus/Jackson delegam ao mesmo método, para que as respostas padrão dessas bibliotecas não mudem o formato.

Exemplo para POST `/mestres` com `{"nome":""}`:

```json
{
  "type": "about:blank",
  "title": "Entrada inválida",
  "status": 400,
  "detail": "nome: Nome deve ser informado.",
  "instance": "/mestres"
}
```

O Content-Type é `application/problem+json`. As mensagens identificam os campos e as regras sem devolver valores privados, consultas SQL ou stack traces.

| Situação | HTTP |
| --- | --- |
| Validação, JSON inválido ou parâmetro inválido | 400 |
| Recurso inexistente | 404 |
| Mestre com escudos, referência em conflito ou duplicidade | 409 |
| Falha interna inesperada | 500 com mensagem genérica e diagnóstico apenas no servidor |

As causas das exceções de persistência são inspecionadas: `23502` (NOT NULL), `23514` (CHECK) e `22001` (texto longo) retornam 400; `23503` (FK), `23505` (unicidade) e `23P01` (exclusão) retornam 409. A conversão também funciona quando o driver está envolvido por uma exceção JPA/transacional.

## Testes

`ValidacaoHttpTest` verifica os pedidos com Bean Validation, a configuração JSON e a conversão segura de estados SQL. Rode `mvn test` para executar os testes sem banco.

Para testar HTTP real e violações reais do PostgreSQL:

```powershell
docker compose up -d
docker compose run --rm --no-deps -T -e SHIELDMAN_TEST_API=true -e SHIELDMAN_TEST_MIGRATIONS=true -e SHIELDMAN_API_URL=http://api:8080 api mvn -B -ntp -Dtest=ApiPostgresTest,MigracoesPostgresTest test
```

`ApiPostgresTest` confere status, Content-Type e campos de problem details para nomes inválidos, JSON quebrado, tipos errados, corpo ausente/nulo, campos extras, UUIDs incorretos e paginação inválida. Seus cadastros temporários são removidos ao final.

`MigracoesPostgresTest` provoca NOT NULL, CHECK, limite de tamanho, unicidade e FK em schemas exclusivos de teste e verifica suas respostas no mapeador. Esses schemas são removidos depois dos testes.
