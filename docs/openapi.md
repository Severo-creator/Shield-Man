# OpenAPI e Swagger UI — T8

A documentação é gerada do código pela extensão `quarkus-smallrye-openapi`. Não há um YAML manual para manter em paralelo.

## Abrir e experimentar

1. Inicie o Docker Desktop e execute `docker compose up -d` na pasta do projeto.
2. Aguarde aparecer `Listening on: http://0.0.0.0:8080` nos logs de `docker compose logs -f api`. Na primeira execução, o download das dependências pode demorar. Use Ctrl+C para sair dos logs; os serviços continuam funcionando.
3. Abra a [Swagger UI](http://localhost:8080/q/swagger-ui/).
4. Expanda `POST /mestres`, clique em **Try it out**, informe `{"nome":"Guilherme"}` e clique em **Execute**. A resposta deve ser `201`, com `Location` e o mestre criado. Copie o `id` recebido.
5. Expanda `POST /escudos` e informe esse UUID em `mestreId`, junto com o nome do escudo. Execute e copie o `id` do escudo criado.
6. Use os UUIDs reais para experimentar GET e PUT. Nas listagens, experimente `nome`, `pagina=0`, `tamanho=10` e, para escudos, `mestreId`.
7. Para excluir esses exemplos, execute primeiro `DELETE /escudos/{id}` e depois `DELETE /mestres/{id}`. A exclusão bem-sucedida retorna `204`, sem corpo. Excluir um mestre que ainda possui escudos retorna `409`.

As ações de **Execute** fazem chamadas à API e podem alterar os dados do banco. Os UUIDs dos exemplos da documentação apenas mostram o formato: substitua-os pelos retornados ao criar os cadastros.

## Endereços e geração

| Recurso | Endereço |
| --- | --- |
| Interface interativa | [Swagger UI](http://localhost:8080/q/swagger-ui/) |
| Especificação YAML | [OpenAPI YAML](http://localhost:8080/q/openapi) |
| Especificação JSON | [OpenAPI JSON](http://localhost:8080/q/openapi?format=json) |

`ApiDocumentacao` define o título, a versão e a descrição. `MestreResource` e `EscudoResource` documentam operações, parâmetros e respostas. Os DTOs descrevem os corpos e limites; `ApiExceptionMapper.Problema` descreve o formato dos erros. As anotações ficam no adaptador HTTP, preservando a independência do domínio.

Todas as 10 operações atuais têm documentação: listar, consultar, criar, atualizar e excluir para mestres e escudos. Ela inclui UUIDs, paginação de até 100 registros, filtros, `201` com `Location`, `204` sem corpo e os erros aplicáveis em `application/problem+json`. Os conteúdos do painel ainda não têm rotas; suas tabelas no banco não geram endpoints automaticamente.

Em `application.properties`, `quarkus.swagger-ui.always-include=true` mantém a UI também na aplicação empacotada. `quarkus.smallrye-openapi.store-schema-directory=target/openapi` exporta a especificação durante o build. As cópias geradas em `target/openapi` não devem ser editadas: altere as classes e gere novamente com `mvn package`.

## Verificar

Com a API já iniciada e respondendo, execute no PowerShell:

```powershell
docker compose run --rm --no-deps -T -e SHIELDMAN_TEST_API=true -e SHIELDMAN_API_URL=http://api:8080 api mvn -B -ntp '-Dtest=OpenApiHttpTest,ApiPostgresTest' test
```

`OpenApiHttpTest` confere a especificação servida contra as operações declaradas no código, os tipos e limites, as respostas de erro, o YAML e a página e os scripts da Swagger UI. `ApiPostgresTest` verifica o comportamento das rotas com o banco real e remove apenas os cadastros que ele próprio cria.

Os testes externos só são habilitados por `SHIELDMAN_TEST_API=true`. `mvn test` continua executando os testes sem exigir uma API em execução.
